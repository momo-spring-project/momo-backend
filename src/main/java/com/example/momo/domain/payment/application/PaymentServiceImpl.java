package com.example.momo.domain.payment.application;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.payment.application.dto.CardPaymentTestRequestDto;
import com.example.momo.domain.payment.application.dto.PaymentResponseDto;
import com.example.momo.domain.payment.application.dto.RefundRequestDto;
import com.example.momo.domain.payment.domain.Payment;
import com.example.momo.domain.payment.domain.PaymentOutbox;
import com.example.momo.domain.payment.domain.PaymentOutboxRepository;
import com.example.momo.domain.payment.domain.PaymentRepository;
import com.example.momo.domain.payment.domain.PaymentService;
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
		// 컨트롤러에서 호출 시 correlation 없이 진행 (내부에서 새 UUID 생성됨)
		return createTestKeyInPayment(request, userId, null);
	}

	@Override
	@Transactional(noRollbackFor = PaymentException.class)
	public PaymentResponseDto createTestKeyInPayment(CardPaymentTestRequestDto request,
		Long userId,
		String correlationUuid) {

		Long meetingId = request.getMeetingId();
		Payment payment = null;

		try {
			// 1) 테스트 키인지 검증
			validateTestKey();

			// 2) 조회
			MeetingClientResponseDto meeting = getMeetingViaClient(meetingId);
			UserClientResponseDto user = getUserViaClient(userId);
			int amount = meeting.getParticipationFee();

			// 3) 결제 검증 / 실패 건 새 row 생성 제거: 기존 row 재사용하여 PENDING 전환
			payment = paymentRepository.findByMeetingIdAndUserId(meeting.getId(), user.getId())
				.map(p -> {
					if (p.getStatus() == PaymentStatus.COMPLETED) {
						throw new PaymentException(PaymentErrorCode.ALREADY_PAID);
					}
					if (p.getStatus() == PaymentStatus.FAILED) {
						p.reopenPending();
					}
					return p; // PENDING이면 그대로 재사용
				})
				.orElseGet(() -> paymentRepository.save(Payment.createPending(user.getId(), meeting.getId(), amount)));

			// 4) 무료 모임 차단
			if (amount == 0) {
				throw new PaymentException(PaymentErrorCode.FREE_MEETING_PARTICIPATION_FAILED);
			}

			// 5) 결제 수행
			String orderId = "order-" + UUID.randomUUID();
			TossKeyInRequestDto tossRequest = buildKeyInRequest(request, meeting, amount, orderId);
			TossPaymentResponseDto result = paymentClient.createTestKeyInPayment(tossRequest, orderId);
			if (!"DONE".equals(result.getStatus())) {
				throw new PaymentException(PaymentErrorCode.TOSS_CONFIRM_FAILED);
			}

			// 6) 완료 처리 + 성공 이벤트
			LocalDateTime approvedAt = OffsetDateTime.parse(result.getApprovedAt()).toLocalDateTime();
			payment.complete(result.getPaymentKey(), orderId, approvedAt);

			Long outboxId = saveOutboxEvent(payment, "PAYMENT_COMPLETED", RoutingKeys.PAYMENT_COMPLETED_KEY,
				correlationUuid);
			eventPublisher.publishEvent(new PaymentEvents.Completed(
				payment.getId(), payment.getUserId(), payment.getMeetingId(), payment.getAmount(), orderId, outboxId));

			log.info("결제 완료 - paymentId: {}, orderId: {}", payment.getId(), orderId);
			return PaymentResponseDto.from(payment);

		} catch (PaymentException pe) {
			// 중복 결제는 이벤트/아웃박스 미발행
			if (pe instanceof PaymentException
				&& pe.getErrorCode() == PaymentErrorCode.ALREADY_PAID) {
				log.info("[중복 결제 요청 무시 - 이벤트 미발행] meetingId={}, userId={}", meetingId, userId);
				// idempotent 성공 응답
				Payment existing = paymentRepository.findByMeetingIdAndUserId(meetingId, userId)
					.orElseThrow(() -> pe);
				return PaymentResponseDto.from(existing);
			}

			// 2) 그 외 비즈니스 예외만 실패 마킹/실패 이벤트 발행
			if (payment != null && payment.getStatus() != PaymentStatus.FAILED) {
				payment.fail(pe.getMessage());
			}

			Long outboxId = saveFailedOutboxEvent(
				payment != null ? payment.getId() : null, meetingId, userId, pe.getMessage(), correlationUuid);

			eventPublisher.publishEvent(new PaymentEvents.Failed(
				payment != null ? payment.getId() : null, userId, meetingId, pe.getMessage(), outboxId));

			throw pe;
		}
	}
	// ==================== 환불 처리 ====================

	/**
	 * 결제 환불 처리 - 환불 후 재결제가 가능하도록 레코드 삭제
	 */
	@Override
	public PaymentResponseDto refundPayment(Long paymentId, Long userId, RefundRequestDto request) {
		// 컨트롤러에서 호출 시 correlation 없이 진행 (내부에서 새 UUID 생성됨)
		return refundPayment(paymentId, userId, request, null);
	}

	@Override
	@Transactional(noRollbackFor = PaymentException.class)
	public PaymentResponseDto refundPayment(Long paymentId, Long userId,
		RefundRequestDto request,
		String correlationUuid) {

		Payment payment = null;

		try {

			payment = paymentRepository.findById(paymentId)
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
				// 외부 4xx/5xx는 여기서 전파됨(WebClientResponseException) -> 시스템 예외이면 롤백
				paymentClient.cancelPayment(payment.getPgTransactionId(), request.getReason());
			}

			// 환불 상태로 변경
			payment.refund();

			// Outbox 저장 및 도메인 이벤트 발행
			Long outboxId = saveOutboxEvent(payment, "PAYMENT_REFUNDED", RoutingKeys.PAYMENT_REFUNDED_KEY,
				request.getReason(), correlationUuid);

			eventPublisher.publishEvent(new PaymentEvents.Refunded(
				payment.getId(),
				payment.getUserId(),
				payment.getMeetingId(),
				payment.getAmount(),
				request.getReason(),
				outboxId
			));

			// 환불 응답 생성
			PaymentResponseDto response = PaymentResponseDto.from(payment);
			// 결제 레코드 삭제 (재결제 가능하도록)
			paymentRepository.delete(payment);

			return response;

		} catch (PaymentException pe) {
			log.warn("[환불 실패-비즈니스] paymentId={}, userId={}, msg={}",
				payment != null ? payment.getId() : null, userId, pe.getMessage());
			recordRefundFailure(payment, payment != null ? payment.getMeetingId() : null, pe.getMessage());
			throw pe;

		} //catch (Exception) 제거 -> 시스템 예외는 롤백
	}

	private Long saveOutboxEvent(Payment payment, String eventType, String routingKey, String correlationUuid) {
		return saveOutboxEvent(payment, eventType, routingKey, null, correlationUuid);
	}

	private Long saveOutboxEvent(Payment payment, String eventType, String routingKey, String refundReason,
		String correlationUuid) {
		try {
			Object eventMessage;

			// 1) 이벤트 DTO 구성 (outboxId는 헤더로 보내므로 DTO 필드는 null로 둬도 됨)
			switch (eventType) {
				case "PAYMENT_COMPLETED" -> eventMessage = new PaymentEventMessages.Completed(
					payment.getId(), payment.getUserId(), payment.getMeetingId(),
					payment.getAmount(),
					payment.getOrderId() != null ? payment.getOrderId() : "",
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
					(refundReason != null && !refundReason.isBlank()) ? refundReason : "환불 사유 미기재",
					null
				);
				default -> throw new IllegalArgumentException("알 수 없는 event type: " + eventType);
			}

			// 2) EventTypeNames와 매핑
			String wrapperType = switch (eventType) {
				case "PAYMENT_COMPLETED" -> EventTypeNames.PAYMENT_COMPLETED; // "payment.completed"
				case "PAYMENT_FAILED" -> EventTypeNames.PAYMENT_FAILED;    // "payment.failed"
				case "PAYMENT_REFUNDED" -> EventTypeNames.PAYMENT_REFUNDED;  // "payment.refunded"
				default -> throw new IllegalArgumentException("알 수 없는 event type: " + eventType);
			};

			// 3) Wrapper로 감싸기(미팅 측에서 보낸 uuid 재사용)
			EventWrapper<Object> wrapper = (correlationUuid != null && !correlationUuid.isBlank())
				? EventWrapper.of(correlationUuid, wrapperType, eventMessage)
				: EventWrapper.of(wrapperType, eventMessage); // fallback 기본값 생성

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

	private Long saveFailedOutboxEvent(Long paymentId, Long meetingId, Long userId, String reason,
		String correlationUuid) {
		try {
			PaymentEventMessages.Failed msg = new PaymentEventMessages.Failed(
				paymentId, userId, meetingId, (reason != null ? reason : "결제 실패"), null
			);

			// ★ 실패도 같은 uuid 유지
			EventWrapper<Object> wrapper = (correlationUuid != null && !correlationUuid.isBlank())
				? EventWrapper.of(correlationUuid, EventTypeNames.PAYMENT_FAILED, msg)
				: EventWrapper.of(EventTypeNames.PAYMENT_FAILED, msg);
			String payload = objectMapper.writeValueAsString(wrapper);

			// Payment 엔티티가 없을 수 있으니, aggregateId를 안전하게 구성
			String aggregateId = (paymentId != null)
				? String.valueOf(paymentId)
				: String.format("meeting:%s/user:%s", String.valueOf(meetingId), String.valueOf(userId));

			PaymentOutbox outbox = PaymentOutbox.create(
				"PAYMENT_FAILED",
				aggregateId,
				RoutingKeys.PAYMENT_FAILED_KEY,
				payload
			);
			PaymentOutbox saved = outboxRepository.save(outbox);
			log.debug("Outbox 실패 이벤트 저장 - aggregateId: {}", aggregateId);
			return saved.getId();
		} catch (Exception e) {
			log.error("Outbox 실패 이벤트 저장 실패", e);
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

	@Override
	@Transactional(readOnly = true)
	public TossPaymentResponseDto getPgPayment(Long paymentId, Long userId) {
		Payment payment = paymentRepository.findById(paymentId)
			.orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

		// 본인 결제만 조회 허용
		if (!payment.getUserId().equals(userId)) {
			throw new PaymentException(PaymentErrorCode.USER_NOT_FOUND);
		}

		// PG 결제키가 없으면(아직 생성 전/삭제됨) 조회 불가
		if (payment.getPgTransactionId() == null || payment.getPgTransactionId().isBlank()) {
			throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND);
		}

		//  토스 단건 조회 호출
		return paymentClient.getPayment(payment.getPgTransactionId());
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

	private void recordRefundFailure(Payment payment, Long meetingId, String errorMessage) {
		Long paymentId = (payment != null ? payment.getId() : null);
		Long userId = (payment != null ? payment.getUserId() : null);

		// 로그만 처리 (관리자 수동 환불로 처리)
		log.error("[환불 실패 기록] paymentId={}, meetingId={}, userId={}, error={}",
			paymentId, meetingId, userId, errorMessage);
	}

}