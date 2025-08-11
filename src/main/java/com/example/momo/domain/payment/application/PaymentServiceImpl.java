package com.example.momo.domain.payment.application;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import com.example.momo.domain.payment.application.dto.CardPaymentTestRequestDto;
import com.example.momo.domain.payment.application.dto.PaymentResponseDto;
import com.example.momo.domain.payment.application.dto.RefundRequestDto;
import com.example.momo.domain.payment.domain.Payment;
import com.example.momo.domain.payment.domain.PaymentOutbox;
import com.example.momo.domain.payment.domain.PaymentOutboxRepository;
import com.example.momo.domain.payment.domain.PaymentRepository;
import com.example.momo.domain.payment.enums.PaymentStatus;
import com.example.momo.domain.payment.event.springEvent.PaymentEvents;
import com.example.momo.domain.payment.exception.PaymentErrorCode;
import com.example.momo.domain.payment.exception.PaymentException;
import com.example.momo.domain.payment.infra.toss.TossPaymentsConfig;
import com.example.momo.global.rabbitmq.constant.EventTypeNames;
import com.example.momo.global.rabbitmq.constant.RoutingKeys;
import com.example.momo.global.rabbitmq.dto.common.EventWrapper;
import com.example.momo.global.rabbitmq.dto.payment.PaymentEventMessages;
import com.example.momo.global.webclient.meeting.MeetingClient;
import com.example.momo.global.webclient.meeting.dto.MeetingClientResponseDto;
import com.example.momo.global.webclient.payment.dto.TossKeyInRequestDto;
import com.example.momo.global.webclient.payment.dto.TossPaymentResponseDto;
import com.example.momo.global.webclient.user.UserClient;
import com.example.momo.global.webclient.user.dto.UserClientResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository paymentRepository;
	private final PaymentOutboxRepository outboxRepository;
	private final PaymentClient paymentClient;
	private final TossPaymentsConfig tossConfig;
	private final ApplicationEventPublisher eventPublisher;
	private final ObjectMapper objectMapper;

	private final MeetingClient meetingClient;
	private final UserClient userClient;

	// ==================== 결제 생성 ====================

	/**
	 * 테스트 Key-in 결제 처리 - 카드번호를 직접 입력하여 결제를 진행하는 테스트 전용 메서드
	 * PENDING 결제가 있으면 재사용, 없으면 새로 생성
	 */
	@Override
	public PaymentResponseDto createTestKeyInPayment(CardPaymentTestRequestDto request, Long userId) {
		// 1. 테스트 환경 검증
		validateTestKey();

		// 2. 엔티티 조회
		MeetingClientResponseDto meeting = getMeetingViaClient(request.getMeetingId());
		UserClientResponseDto user = getUserViaClient(userId);
		int amount = meeting.getParticipationFee();

		// 3. 기존 결제 상태 확인
		// 3-1. 이미 완료된 결제가 있으면 예외
		if (paymentRepository.existsByMeetingIdAndUserIdAndStatus(
			meeting.getId(), user.getId(), PaymentStatus.COMPLETED)) {
			throw new PaymentException(PaymentErrorCode.ALREADY_PAID);
		}

		// 3-2. PENDING 또는 FAILED 결제 조회
		Payment payment = paymentRepository.findByMeetingIdAndUserId(
				meeting.getId(), user.getId())
			.filter(p -> p.getStatus() == PaymentStatus.PENDING || p.getStatus() == PaymentStatus.FAILED)
			.orElseGet(() -> {
				// PENDING이 없으면 새로 생성 (예외 케이스 대비)
				Payment newPayment = Payment.createPending(user.getId(), meeting.getId(),
					amount);
				return paymentRepository.save(newPayment);
			});

		// FAILED 상태인 경우 PENDING으로 변경
		if (payment.getStatus() == PaymentStatus.FAILED) {
			payment = Payment.createPending(userId, meeting.getId(), amount);
			payment = paymentRepository.save(payment);
		}

		// 4. 무료 모임인 경우 결제 차단
		if (amount == 0) {
			log.warn("무료 모임에 대한 결제 요청이 차단되었습니다. meetingId: {}, userId: {}",
				meeting.getId(), user.getId());
			throw new PaymentException(PaymentErrorCode.FREE_MEETING_PARTICIPATION_FAILED);
		}

		// 5. 유료 결제 처리
		String orderId = "order-" + UUID.randomUUID();

		try {
			// Key-in API 호출
			TossKeyInRequestDto tossRequest = buildKeyInRequest(request, meeting, amount, orderId);
			TossPaymentResponseDto result = paymentClient.createTestKeyInPayment(tossRequest, orderId);

			if (!"DONE".equals(result.getStatus())) {
				throw new PaymentException(PaymentErrorCode.TOSS_CONFIRM_FAILED);
			}

			// 결제 완료 처리
			LocalDateTime approvedAt = OffsetDateTime.parse(result.getApprovedAt()).toLocalDateTime();
			payment.complete(result.getPaymentKey(), orderId, approvedAt);

			// Outbox 저장 및 도메인 이벤트 발행
			Long outboxId = saveOutboxEvent(payment, "PAYMENT_COMPLETED", RoutingKeys.PAYMENT_COMPLETED_KEY);

			eventPublisher.publishEvent(new PaymentEvents.Completed(
				payment.getId(),
				payment.getUserId(),
				payment.getMeetingId(),
				payment.getAmount(),
				orderId,
				outboxId
			));

			log.info("결제 완료 - paymentId: {}, orderId: {}", payment.getId(), orderId);
			return PaymentResponseDto.from(payment);

		} catch (Exception e) {
			// 결제 실패 처리
			payment.fail(e.getMessage());

			// Outbox 저장 및 도메인 이벤트 발행
			Long outboxId = saveOutboxEvent(payment, "PAYMENT_FAILED", RoutingKeys.PAYMENT_FAILED_KEY);
			eventPublisher.publishEvent(new PaymentEvents.Failed(
				payment.getId(),
				payment.getUserId(),
				payment.getMeetingId(),
				e.getMessage(),
				outboxId
			));

			throw new PaymentException(PaymentErrorCode.TOSS_CONFIRM_FAILED);
		}
	}

	// ==================== 환불 처리 ====================

	/**
	 * 결제 환불 처리 - 환불 후 재결제가 가능하도록 레코드 삭제
	 */
	@Override
	public PaymentResponseDto refundPayment(Long paymentId, Long userId, RefundRequestDto request) {
		Payment payment = paymentRepository.findById(paymentId)
			.orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

		if (!payment.getUserId().equals(userId)) {
			throw new PaymentException(PaymentErrorCode.UNAUTHORIZED_REFUND);
		}

		// 환불 가능 상태 확인
		if (payment.getStatus() != PaymentStatus.COMPLETED) {
			throw new PaymentException(PaymentErrorCode.REFUND_NOT_ALLOWED);
		}

		// 토스 환불 처리
		if ("TOSS".equalsIgnoreCase(payment.getPaymentMethod())) {
			try {
				paymentClient.cancelPayment(payment.getPgTransactionId(), request.getReason());
			} catch (HttpClientErrorException e) {
				log.error("[TOSS] 환불 실패: {}", e.getResponseBodyAsString());
				throw new PaymentException(PaymentErrorCode.REFUND_FAILED);
			}
		}

		// 환불 상태로 변경
		payment.refund();

		// 환불 응답 생성
		PaymentResponseDto response = PaymentResponseDto.from(payment);

		// Outbox 저장 및 도메인 이벤트 발행
		Long outboxId = saveOutboxEvent(payment, "PAYMENT_REFUNDED", RoutingKeys.PAYMENT_REFUNDED_KEY);
		eventPublisher.publishEvent(new PaymentEvents.Refunded(
			payment.getId(),
			payment.getUserId(),
			payment.getMeetingId(),
			payment.getAmount(),
			request.getReason(),
			outboxId
		));

		// 결제 레코드 삭제 (재결제 가능하도록)
		paymentRepository.delete(payment);

		return response;
	}

	private Long saveOutboxEvent(Payment payment, String eventType, String routingKey) {
		try {
			Object eventMessage;
			// 1) 이벤트 DTO 구성 (outboxId는 헤더로 보내므로 DTO 필드는 null로 둬도 됨)
			switch (eventType) {
				case "PAYMENT_COMPLETED" -> eventMessage = new PaymentEventMessages.Completed(
					payment.getId(), payment.getUserId(), payment.getMeetingId(),
					payment.getAmount(),
					payment.getOrderId() != null ? payment.getOrderId() : "", // orderId가 필요하면 저장
					null
				);
				case "PAYMENT_FAILED" -> eventMessage = new PaymentEventMessages.Failed(
					payment.getId(), payment.getUserId(), payment.getMeetingId(),
					payment.getFailReason() != null ? payment.getFailReason() : "결제 실패",
					null
				);
				case "PAYMENT_REFUNDED" -> eventMessage = new PaymentEventMessages.Refunded(
					payment.getId(), payment.getUserId(), payment.getMeetingId(),
					payment.getAmount(),
					"환불 사유", // 필요 시 실제 사유로 교체
					null
				);
				default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
			}

			// 2) EventTypeNames와 매핑
			String wrapperType = switch (eventType) {
				case "PAYMENT_COMPLETED" -> EventTypeNames.PAYMENT_COMPLETED; // "payment.completed"
				case "PAYMENT_FAILED" -> EventTypeNames.PAYMENT_FAILED;    // "payment.failed"
				case "PAYMENT_REFUNDED" -> EventTypeNames.PAYMENT_REFUNDED;  // "payment.refunded"
				default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
			};

			// 3) Wrapper로 감싸기
			EventWrapper<Object> wrapper = EventWrapper.of(wrapperType, eventMessage);

			// 4) Outbox payload에 Wrapper JSON 저장
			String payload = objectMapper.writeValueAsString(wrapper);

			PaymentOutbox outbox = PaymentOutbox.create(
				eventType,
				payment.getId() != null ? payment.getId().toString() : "TEMP",
				routingKey,
				payload
			);
			PaymentOutbox saved = outboxRepository.save(outbox);
			log.debug("Outbox 이벤트 저장(Wrapper) - type: {}, paymentId: {}", eventType, payment.getId());

			return saved.getId();
		} catch (Exception e) {
			log.error("Outbox 저장 실패", e);
			throw new RuntimeException("이벤트 저장 실패", e);
		}
	}
	// ==================== 조회 메서드 ====================

	@Override
	@Transactional(readOnly = true)
	public Page<PaymentResponseDto> getMyPayments(Long userId, PaymentStatus status, Pageable pageable) {
		Page<Payment> page = paymentRepository.searchMyPayments(userId, status, pageable);
		return page.map(PaymentResponseDto::from);
	}

	/**
	 * 모임별 결제 내역 조회
	 */
	@Override
	@Transactional(readOnly = true)
	public List<PaymentResponseDto> getPaymentsByMeetingId(Long meetingId) {
		// 모임 존재 여부 확인 (Client 사용)
		MeetingClientResponseDto meeting = meetingClient.getMeeting(meetingId);
		if (meeting == null) {
			throw new PaymentException(PaymentErrorCode.MEETING_NOT_FOUND);
		}

		return paymentRepository.findByMeetingId(meetingId)
			.stream()
			.map(PaymentResponseDto::from)
			.collect(Collectors.toList());
	}

	/**
	 * 사용자별 결제 내역 조회
	 */
	@Override
	@Transactional(readOnly = true)
	public List<PaymentResponseDto> getPaymentsByUserId(Long userId) {
		// 사용자 존재 여부 확인 (Client 사용)
		UserClientResponseDto user = userClient.getUser(userId);
		if (user == null) {
			throw new PaymentException(PaymentErrorCode.USER_NOT_FOUND);
		}
		return paymentRepository.findByUserId(userId)
			.stream()
			.map(PaymentResponseDto::from)
			.collect(Collectors.toList());
	}

	/**
	 * 사용자의 모임 결제 여부 확인
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean validateUserPayment(Long userId, Long meetingId) {
		return paymentRepository.existsByMeetingIdAndUserIdAndStatus(
			meetingId, userId, PaymentStatus.COMPLETED);
	}

	// /**
	//  * 관리자용 전체 조회 메서드
	//  */
	// @Override
	// @Transactional(readOnly = true)
	// public Page<PaymentResponseDto> searchPayments(Long meetingId,
	// 	Long userId,
	// 	PaymentStatus status,
	// 	Pageable pageable) {
	//
	// 	Page<Payment> page = paymentRepository.searchPayments(meetingId, userId, status, pageable);
	// 	return page.map(PaymentResponseDto::from);
	// }

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
	 * 모임 조회 (Client 사용)
	 */
	private MeetingClientResponseDto getMeetingViaClient(Long meetingId) {
		MeetingClientResponseDto meeting = meetingClient.getMeeting(meetingId);
		if (meeting == null) {
			throw new PaymentException(PaymentErrorCode.MEETING_NOT_FOUND);
		}
		return meeting;
	}

	/**
	 * 사용자 조회 (Client 사용)
	 */
	private UserClientResponseDto getUserViaClient(Long userId) {
		UserClientResponseDto user = userClient.getUser(userId);
		if (user == null) {
			throw new PaymentException(PaymentErrorCode.USER_NOT_FOUND);
		}
		return user;
	}

	/**
	 * Key-in API 요청 페이로드 생성
	 */
	private TossKeyInRequestDto buildKeyInRequest(CardPaymentTestRequestDto request,
		MeetingClientResponseDto meeting,
		int amount, String orderId) {

		String[] exp = (request.getCardExpiry() != null ? request.getCardExpiry() : "12/25").split("/");
		String expMonth = exp[0];   // "12"
		String expYear = exp[1];   // "25"

		return TossKeyInRequestDto.builder()
			.amount(amount)
			.orderId(orderId)
			.orderName(meeting.getTitle() + " 참가비")
			.cardNumber(request.getCardNumber() != null ? request.getCardNumber() : "4242424242424242")
			.cardExpirationYear(expYear)
			.cardExpirationMonth(expMonth)
			.cardPassword("12")
			.customerIdentityNumber(request.getBirth() != null ? request.getBirth() : "880101")
			.customerEmail("test@example.com")
			.customerName("테스트")
			.build();
	}
}