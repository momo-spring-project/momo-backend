package com.example.momo.domain.payments.application;

import com.example.momo.domain.meetings.domain.Meeting;
import com.example.momo.domain.meetings.domain.MeetingRepository;
import com.example.momo.domain.payments.domain.Payment;
import com.example.momo.domain.payments.dto.CardPaymentTestRequest;
import com.example.momo.domain.payments.dto.PaymentResponse;
import com.example.momo.domain.payments.dto.RefundRequest;
import com.example.momo.domain.payments.enums.PaymentStatus;
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

    if (paymentRepository.existsByMeetingIdAndUserIdAndStatus(
        meeting.getId(), user.getId(), PaymentStatus.COMPLETED)) {
      throw new IllegalStateException("이미 결제가 완료된 사용자입니다.");
    }

    // 3. 무료 모임인 경우 별도 처리
    if (amount == 0) {
      return createFreePayment(user, meeting);
    }

    // 4. orderId 미리 생성
    String orderId = "order-" + UUID.randomUUID();

    // 5. 토스 Key-in API 호출
    Map<String, Object> payload = buildKeyInPayload(request, meeting, amount);
    Map<String, Object> keyInResult = tossClient.createTestKeyInPayment(payload, orderId);
    String paymentKey = (String) keyInResult.get("paymentKey");
    String status = (String) keyInResult.get("status");

    // 6. Toss 결제 상태에 따라 처리
    if ("READY".equals(status) || "WAITING_FOR_CONFIRMATION".equals(status)) {
      try {
        tossClient.confirmPayment(paymentKey, orderId, amount);
      } catch (HttpClientErrorException e) {
        log.error("[TOSS] confirmPayment 실패: {}", e.getResponseBodyAsString());
        throw new IllegalStateException("결제 승인 실패: " + e.getResponseBodyAsString());
      }
    } else if (!"DONE".equals(status)) {
      throw new IllegalStateException("지원하지 않는 결제 상태입니다: " + status);
    }

    // 7. 결제 정보 저장
    return savePaidPayment(user, meeting, amount, paymentKey, orderId);
  }

  // ==================== 환불 처리 ====================

  /**
   * 결제 환불 처리
   */
  @Override
  public PaymentResponse refundPayment(Long paymentId, RefundRequest request) {
    Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

    // 토스 결제인 경우 토스 API를 통해 환불
    if ("TOSS".equalsIgnoreCase(payment.getPaymentMethod())) {
      tossClient.cancelPayment(payment.getPgTransactionId(), request.getReason());
    }

    payment.refund();
    return PaymentResponse.from(paymentRepository.save(payment));
  }

  // ==================== 조회 메서드 ====================

  /**
   * 모임별 결제 내역 조회
   */
  @Override
  @Transactional(readOnly = true)
  public List<PaymentResponse> getPaymentsByMeetingId(Long meetingId) {
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
      throw new IllegalStateException("Key-in API는 테스트 키에서만 호출할 수 있습니다");
    }
  }

  /**
   * 모임 조회
   */
  private Meeting getMeeting(Long meetingId) {
    return meetingRepository.findById(meetingId)
        .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));
  }

  /**
   * 사용자 조회
   */
  private User getUser(Long userId) {
    return userRepository.findByIdAndIsDeletedFalse(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
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
      int amount) {

    String[] exp = (req.getCardExpiry() != null ? req.getCardExpiry() : "12/25").split("/");
    String expMonth = exp[0];   // "12"
    String expYear = exp[1];   // "25"

    String orderId = "order-" + UUID.randomUUID(); // UUID로 고유한 주문 번호 생성

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