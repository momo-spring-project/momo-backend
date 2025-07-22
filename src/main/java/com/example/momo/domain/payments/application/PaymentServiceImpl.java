package com.example.momo.domain.payments.application;

import com.example.momo.domain.meetings.domain.Meeting;
import com.example.momo.domain.meetings.domain.MeetingRepository;
import com.example.momo.domain.payments.domain.Payment;
import com.example.momo.domain.payments.dto.CardPaymentTestRequest;
import com.example.momo.domain.payments.dto.PaymentResponse;
import com.example.momo.domain.payments.dto.RefundRequest;
import com.example.momo.domain.payments.enums.PaymentStatus;
import com.example.momo.domain.payments.exception.PaymentErrorCode;
import com.example.momo.domain.payments.exception.PaymentException;
import com.example.momo.domain.payments.infra.PaymentRepository;
import com.example.momo.domain.payments.infra.toss.TossPaymentsClient;
import com.example.momo.domain.payments.infra.toss.TossPaymentsConfig;
import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.infra.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final MeetingRepository meetingRepository;
  private final UserRepository userRepository;
  private final TossPaymentsClient tossClient;
  private final TossPaymentsConfig tossConfig;

  // ==================== 결제 생성 ====================

  /**
   * 테스트 Key-in 결제 처리 - 카드번호를 직접 입력하여 결제를 진행하는 테스트 전용 메서드
   */
  @Override
  public PaymentResponse createTestKeyInPayment(CardPaymentTestRequest request) {
    // 1. 테스트 환경 검증
    validateTestKey();

    // 2. 엔티티 조회
    Meeting meeting = getMeeting(request.getMeetingId());
    User user = getUser(request.getUserId());
    int amount = meeting.getParticipationFee();

    // 3. 중복 결제 확인
    if (paymentRepository.existsByMeetingIdAndUserIdAndStatus(
        meeting.getId(), user.getId(), PaymentStatus.COMPLETED)) {
      throw new PaymentException(PaymentErrorCode.ALREADY_PAID);
    }

    // 4. 무료 모임인 경우 별도 처리
    if (amount == 0) {
      return createFreePayment(user, meeting);
    }

    // 5. orderId 미리 생성
    String orderId = "order-" + UUID.randomUUID();

    // 6. 토스 Key-in API 호출
    Map<String, Object> payload = buildKeyInPayload(request, meeting, amount, orderId);
    Map<String, Object> keyInResult;

    try {
      keyInResult = tossClient.createTestKeyInPayment(payload, orderId);
    } catch (HttpClientErrorException e) {
      log.error("[TOSS] Key-in 결제 생성 실패: {}", e.getResponseBodyAsString());
      throw new PaymentException(PaymentErrorCode.TOSS_CONFIRM_FAILED);
    }

    String paymentKey = (String) keyInResult.get("paymentKey");
    String status = (String) keyInResult.get("status");

    // 7. Toss 결제 상태에 따라 처리
    if ("READY".equals(status) || "WAITING_FOR_CONFIRMATION".equals(status)) {
      try {
        tossClient.confirmPayment(paymentKey, orderId, amount);
      } catch (HttpClientErrorException e) {
        log.error("[TOSS] 결제 승인 실패: {}", e.getResponseBodyAsString());
        throw new PaymentException(PaymentErrorCode.TOSS_CONFIRM_FAILED);
      }
    } else if (!"DONE".equals(status)) {
      log.error("[TOSS] 지원하지 않는 결제 상태: {}", status);
      throw new PaymentException(PaymentErrorCode.UNSUPPORTED_PAYMENT_STATUS);
    }

    // 8. 결제 정보 저장
    return savePaidPayment(user, meeting, amount, paymentKey, orderId);
  }

  // ==================== 환불 처리 ====================

  /**
   * 결제 환불 처리 - 환불 후 재결제가 가능하도록 레코드 삭제
   */
  @Override
  public PaymentResponse refundPayment(Long paymentId, RefundRequest request) {
    Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

    // 환불 가능 상태 확인
    if (payment.getStatus() != PaymentStatus.COMPLETED) {
      throw new PaymentException(PaymentErrorCode.REFUND_NOT_ALLOWED);
    }

    // 토스 결제인 경우 토스 API를 통해 환불
    if ("TOSS".equalsIgnoreCase(payment.getPaymentMethod())) {
      try {
        tossClient.cancelPayment(payment.getPgTransactionId(), request.getReason());
      } catch (HttpClientErrorException e) {
        log.error("[TOSS] 환불 실패: {}", e.getResponseBodyAsString());
        throw new PaymentException(PaymentErrorCode.REFUND_FAILED);
      }
    }
    // 환불 상태로 변경
    payment.refund();

    // 환불 응답 생성 (삭제 전에 생성)
    PaymentResponse response = PaymentResponse.from(payment);

    // 결제 레코드 삭제 (재결제 가능하도록)
    paymentRepository.delete(payment);
    log.info("결제 환불 및 삭제 완료 - paymentId: {}, userId: {}, meetingId: {}",
        payment.getId(), payment.getUserId(), payment.getMeetingId());

    return response;
  }

  // ==================== 조회 메서드 ====================

  /**
   * 모임별 결제 내역 조회
   */
  @Override
  @Transactional(readOnly = true)
  public List<PaymentResponse> getPaymentsByMeetingId(Long meetingId) {
    // 모임 존재 여부 확인
    if (!meetingRepository.existsById(meetingId)) {
      throw new PaymentException(PaymentErrorCode.MEETING_NOT_FOUND);
    }

    return paymentRepository.findByMeetingId(meetingId)
        .stream()
        .map(PaymentResponse::from)
        .collect(Collectors.toList());
  }

  /**
   * 사용자별 결제 내역 조회
   */
  @Override
  @Transactional(readOnly = true)
  public List<PaymentResponse> getPaymentsByUserId(Long userId) {
//    // 사용자 존재 여부 확인
//    if (!userRepository.existsByIdAndIsDeletedFalse(userId)) {
//      throw new PaymentException(PaymentErrorCode.USER_NOT_FOUND);
//    }
    return paymentRepository.findByUserId(userId)
        .stream()
        .map(PaymentResponse::from)
        .collect(Collectors.toList());
  }

  /**
   * 사용자의 모임 결제 여부 확인
   */
  @Override
  @Transactional(readOnly = true)
  public boolean hasUserPaidForMeeting(Long userId, Long meetingId) {
    return paymentRepository.existsByMeetingIdAndUserIdAndStatus(
        meetingId, userId, PaymentStatus.COMPLETED);
  }

  // ==================== Private Helper 메서드 ====================

  /**
   * 테스트 키 검증
   */
  private void validateTestKey() {
    if (!tossConfig.getSecretKey().startsWith("test_")) {
      throw new PaymentException(PaymentErrorCode.TEST_KEY_ONLY);
    }
  }

  /**
   * 모임 조회
   */
  private Meeting getMeeting(Long meetingId) {
    return meetingRepository.findById(meetingId)
        .orElseThrow(() -> new PaymentException(PaymentErrorCode.MEETING_NOT_FOUND));
  }

  /**
   * 사용자 조회
   */
  private User getUser(Long userId) {
    return userRepository.findByIdAndIsDeletedFalse(userId)
        .orElseThrow(() -> new PaymentException(PaymentErrorCode.USER_NOT_FOUND));
  }

  /**
   * 무료 결제 생성
   */
  private PaymentResponse createFreePayment(User user, Meeting meeting) {
    Payment payment = Payment.createFree(user.getId(), meeting.getId());
    Payment saved = paymentRepository.save(payment);
    log.info("무료 결제 완료 - paymentId: {}", saved.getId());
    return PaymentResponse.from(saved);
  }

  /**
   * Key-in API 요청 페이로드 생성
   */
  private Map<String, Object> buildKeyInPayload(CardPaymentTestRequest req,
      Meeting meeting,
      int amount, String orderId) {

    String[] exp = (req.getCardExpiry() != null ? req.getCardExpiry() : "12/25").split("/");
    String expMonth = exp[0];   // "12"
    String expYear = exp[1];   // "25"

    return Map.of(
        "amount", amount,
        "orderId", orderId,
        "orderName", meeting.getTitle() + " 참가비",
        "cardNumber", req.getCardNumber() != null ? req.getCardNumber() : "4242424242424242",
        "cardExpirationYear", expYear,
        "cardExpirationMonth", expMonth,
        "cardPassword", "12",                      // 카드 비밀번호 앞 2자리
        "customerIdentityNumber", req.getBirth() != null ? req.getBirth() : "880101",
        "customerEmail", "test@example.com",
        "customerName", "테스트"
    );
  }

  /**
   * 유료 결제 정보 저장
   */
  private PaymentResponse savePaidPayment(User user, Meeting meeting, int amount,
      String paymentKey, String orderId) {
    Payment payment = Payment.builder()
        .userId(user.getId())
        .meetingId(meeting.getId())
        .amount(amount)
        .paymentMethod("TOSS")
        .pgTransactionId(paymentKey)
        .orderId(orderId)
        .status(PaymentStatus.COMPLETED)
        .paidAt(LocalDateTime.now())
        .build();

    Payment saved = paymentRepository.save(payment);
    log.info("Key-in 결제 완료 - paymentId: {}, paymentKey: {}", saved.getId(), paymentKey);
    return PaymentResponse.from(saved);
  }
}