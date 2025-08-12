package com.example.momo.domain.payment.event.rabbitmq.consumer;

import static com.example.momo.global.rabbitmq.constant.EventTypeNames.*;
import static com.example.momo.global.rabbitmq.constant.QueueNames.*;

import java.util.List;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.momo.domain.payment.application.PaymentService;
import com.example.momo.domain.payment.application.dto.CardPaymentTestRequestDto;
import com.example.momo.domain.payment.application.dto.RefundRequestDto;
import com.example.momo.domain.payment.domain.Payment;
import com.example.momo.domain.payment.domain.PaymentRepository;
import com.example.momo.domain.payment.enums.PaymentStatus;
import com.example.momo.domain.payment.exception.PaymentException;
import com.example.momo.global.rabbitmq.dto.common.EventWrapper;
import com.example.momo.global.rabbitmq.dto.meeting.ParticipantEvents;
import com.example.momo.global.springEvent.meeting.MeetingMessageEvents;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Payment 도메인의 RabbitMQ 이벤트 Consumer
 * - 참가자 등록/취소 이벤트 처리
 * - 모임 삭제 이벤트 처리
 *
 * 처리 정책:
 *  - 성공/멱등: 직접 ACK
 *  - 비즈니스 예외(PaymentException): ACK (재처리 불필요, 실패 이벤트는 서비스 내부 outbox로 발행)
 *  - 즉시 DLQ(재시도 불필요): AmqpRejectAndDontRequeueException 던짐
 *      - NULL, 타입 불일치, 역직렬화 실패, 필수 필드 누락
 *  - 시스템/일시적 오류: RuntimeException 던짐 -> 컨테이너 재시도 후 DLQ
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

	private final PaymentRepository paymentRepository;
	private final PaymentService paymentService;
	private final ObjectMapper objectMapper;

	/**
	 * 1) 참가자 등록 -> 결제 생성
	 */
	@RabbitListener(
		queues = PAYMENT_PARTICIPANT_REGISTER,
		containerFactory = "paymentListenerContainerFactory"
	)
	public void handleParticipantRegister(EventWrapper<?> wrapper, Channel ch, Message msg) {
		long tag = msg.getMessageProperties().getDeliveryTag();
		Long meetingId = null, userId = null;

		// NULL payload: ack 후 드랍
		if (wrapper == null || wrapper.data() == null) {
			log.warn("[register] null payload - ack & drop (no dlq) ");
			safeAck(ch, tag);
			return;
		}
		try {

			// 즉시 DLQ: 타입 불일치
			if (!MEETING_PARTICIPANT_REGISTER.equals(wrapper.type())) {
				throw new AmqpRejectAndDontRequeueException(
					"[register] wrong type: " + wrapper.type());
			}

			// 즉시 DLQ: 역직렬화 실패
			final ParticipantEvents.Register ev;
			try {
				ev = objectMapper.convertValue(wrapper.data(), ParticipantEvents.Register.class);
			} catch (Exception cvt) {
				throw new AmqpRejectAndDontRequeueException("[register] deserialize fail", cvt);
			}

			meetingId = ev.meetingId();
			userId = ev.userId();

			// 즉시 DLQ: 필수 필드 누락
			if (meetingId == null || userId == null) {
				throw new AmqpRejectAndDontRequeueException("[register] missing fields");
			}

			// 결제 비즈니스
			log.info("[결제 시작] meetingId={}, userId={}", meetingId, userId);
			CardPaymentTestRequestDto request = CardPaymentTestRequestDto.builder()
				.meetingId(meetingId).build();
			String corr = wrapper.uuId(); //Register의 uuid
			paymentService.createTestKeyInPayment(request, userId, corr);

			// 성공 -> ACK
			ch.basicAck(tag, false);
			log.info("[결제 완료] meetingId={}, userId={}", meetingId, userId);

		} catch (PaymentException be) {
			// 비즈니스 예외 -> ACK
			log.warn("[결제 비즈니스 예외] meetingId={}, userId={}, err={}", meetingId, userId, be.getMessage());
			safeAck(ch, tag);

		} catch (AmqpRejectAndDontRequeueException reject) {
			// 즉시 DLQ
			throw reject;

		} catch (Exception ex) {
			// 시스템/일시적 오류 -> 재시도 후 DLQ
			throw new RuntimeException(ex);
		}
	}

	/**
	 * 2) 참가자 취소 -> 개별 환불
	 */
	@RabbitListener(
		queues = PAYMENT_PARTICIPANT_CANCEL,
		containerFactory = "paymentListenerContainerFactory"
	)
	public void handleParticipantCancel(EventWrapper<?> wrapper, Channel ch, Message msg) {
		long tag = msg.getMessageProperties().getDeliveryTag();

		// NULL payload: ack 후 드랍
		if (wrapper == null || wrapper.data() == null) {
			log.warn("[cancel] null payload - ack & drop (no dlq)");
			safeAck(ch, tag);
			return;
		}

		try {

			// 즉시 DLQ: 타입 불일치
			if (!MEETING_PARTICIPANT_CANCEL.equals(wrapper.type())) {
				throw new AmqpRejectAndDontRequeueException("[cancel] wrong type: " + wrapper.type());
			}

			// 즉시 DLQ: 역직렬화 실패
			final ParticipantEvents.Cancel ev;
			try {
				ev = objectMapper.convertValue(wrapper.data(), ParticipantEvents.Cancel.class);
			} catch (Exception cvt) {
				throw new AmqpRejectAndDontRequeueException("[cancel] deserialize fail", cvt);
			}

			// 즉시 DLQ: 필수 필드 누락
			if (ev.meetingId() == null || ev.userId() == null) {
				throw new AmqpRejectAndDontRequeueException("[cancel] missing fields");
			}

			log.info("[환불 시작] meetingId={}, userId={}, refundRequired={}",
				ev.meetingId(), ev.userId(), ev.refundRequired());

			// 무료/환불 불필요 -> ACK
			if (!ev.refundRequired()) {
				ch.basicAck(tag, false);
				return;
			}

			// 완료 결제 조회
			Payment payment = paymentRepository.findByMeetingIdAndUserIdAndStatus(
				ev.meetingId(), ev.userId(), PaymentStatus.COMPLETED).orElse(null);

			// 환불할 결제 없음 -> ACK
			if (payment == null) {
				log.warn("[환불] no completed payment. meetingId={}, userId={}", ev.meetingId(), ev.userId());
				ch.basicAck(tag, false);
				return;
			}

			// 환불 처리
			RefundRequestDto refundRequest = new RefundRequestDto(
				String.format("사용자 %s님의 참가 취소", ev.participantNickname()));
			String corr = wrapper.uuId();
			paymentService.refundPayment(payment.getId(), ev.userId(), refundRequest, corr);

			// 성공 -> ACK
			ch.basicAck(tag, false);
			log.info("[환불 완료] paymentId={}, amount={}", payment.getId(), payment.getAmount());

		} catch (PaymentException be) {
			// 비즈니스 예외 -> ACK
			log.warn("[환불 비즈니스 예외] {}", be.getMessage());
			safeAck(ch, tag);

		} catch (AmqpRejectAndDontRequeueException reject) {
			// 즉시 DLQ
			throw reject;

		} catch (Exception ex) {
			// 시스템/일시적 오류 -> 재시도 후 DLQ
			throw new RuntimeException(ex);
		}
	}

	/**
	 * 3) 모임 삭제 -> 전체 환불
	 */
	@RabbitListener(
		queues = PAYMENT_MEETING_DELETED,
		containerFactory = "paymentListenerContainerFactory"
	)
	public void handleMeetingDeleted(MeetingMessageEvents.Delete event, Channel ch, Message msg) {
		long tag = msg.getMessageProperties().getDeliveryTag();

		try {
			// 즉시 DLQ: NULL/필수 누락
			if (event == null || event.meetingId() == null) {
				throw new AmqpRejectAndDontRequeueException("[meeting.deleted] null event or missing meetingId");
			}

			log.info("[모임 삭제 환불] meetingId={}, meetingName={}, 참가자={}명",
				event.meetingId(), event.meetingName(),
				event.userIdList() != null ? event.userIdList().size() : 0);

			List<Payment> completed = paymentRepository.findByMeetingIdAndStatus(
				event.meetingId(), PaymentStatus.COMPLETED);

			if (completed.isEmpty()) {
				ch.basicAck(tag, false);
				return;
			}

			int ok = 0, fail = 0;
			for (Payment payment : completed) {
				try {
					RefundRequestDto refundRequest = new RefundRequestDto(
						String.format("모임 '%s' 삭제로 인한 자동 환불", event.meetingName()));
					paymentService.refundPayment(payment.getId(), payment.getUserId(), refundRequest);
					ok++;
				} catch (Exception refundEx) {
					fail++;
					log.error("[환불 실패] paymentId={}, err={}", payment.getId(), refundEx.getMessage(), refundEx);
					recordRefundFailure(payment, event.meetingId(), refundEx.getMessage());
				}
			}

			log.info("[모임 삭제 환불 완료] 성공:{}건, 실패:{}건", ok, fail);
			// 부분 실패여도 ACK (환불 실패건은 별도 관리)
			ch.basicAck(tag, false);

		} catch (AmqpRejectAndDontRequeueException reject) {
			throw reject;
		} catch (Exception ex) {
			// 시스템/일시적 오류 -> 재시도 후 DLQ
			throw new RuntimeException(ex);
		}
	}

	// ===== helpers =====

	private void recordRefundFailure(Payment payment, Long meetingId, String errorMessage) {
		// TODO: RefundFailureRecord 엔티티 저장 및 관리자 알림
		log.error("[환불 실패 기록] paymentId={}, meetingId={}, userId={}, error={}",
			payment.getId(), meetingId, payment.getUserId(), errorMessage);
	}

	private void safeAck(Channel ch, long tag) {
		try {
			ch.basicAck(tag, false);
		} catch (Exception e) {
			log.error("ACK 실패", e);
		}
	}
}
