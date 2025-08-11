package com.example.momo.domain.payment.event.rabbitmq.consumer;

import static com.example.momo.global.rabbitmq.constant.QueueNames.*;

import java.io.IOException;
import java.util.List;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.momo.domain.payment.application.PaymentService;
import com.example.momo.domain.payment.application.dto.CardPaymentTestRequestDto;
import com.example.momo.domain.payment.application.dto.RefundRequestDto;
import com.example.momo.domain.payment.domain.Payment;
import com.example.momo.domain.payment.domain.PaymentRepository;
import com.example.momo.domain.payment.enums.PaymentStatus;
import com.example.momo.global.rabbitmq.dto.ParticipantEvents;
import com.example.momo.global.springEvent.meeting.MeetingMessageEvents;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Payment 도메인의 RabbitMQ 이벤트 Consumer
 * - 참가자 등록/취소 이벤트 처리
 * - 모임 삭제 이벤트 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

	private final PaymentRepository paymentRepository;
	private final PaymentService paymentService;

	/**
	 * 1. 참가자 등록 이벤트 처리 - 결제 생성
	 * Queue: payment.participant.registered.queue
	 * Event: ParticipantEvents.Register (meetingId, userId)
	 *
	 * Meeting에서 참가 신청 시 발행하는 이벤트
	 * 결제 처리 후 payment.completed 또는 payment.failed 이벤트 발행
	 */
	@RabbitListener(
		queues = PAYMENT_PARTICIPANT_REGISTER,
		containerFactory = "paymentListenerContainerFactory"
	)
	public void handleParticipantRegister(ParticipantEvents.Register event,
		Channel channel,
		Message message) {
		long deliveryTag = message.getMessageProperties().getDeliveryTag();

		log.info("[결제 시작] 참가자 등록 이벤트 수신 - meetingId: {}, userId: {}",
			event.meetingId(), event.userId());

		try {
			// 이미 결제 완료된 경우 체크 (중복 방지)
			if (paymentRepository.existsByMeetingIdAndUserIdAndStatus(
				event.meetingId(), event.userId(), PaymentStatus.COMPLETED)) {
				log.warn("이미 결제 완료됨 – meetingId={}, userId={}",
					event.meetingId(), event.userId());
				channel.basicAck(deliveryTag, false);
				return;
			}

			// 결제 실행 (Service에서 결제 생성 및 처리)
			// createTestKeyInPayment 내부에서:
			// - 성공 시: payment.completed 이벤트 발행
			// - 실패 시: payment.failed 이벤트 발행
			CardPaymentTestRequestDto request = CardPaymentTestRequestDto.builder()
				.meetingId(event.meetingId())
				.build();

			paymentService.createTestKeyInPayment(request, event.userId());

			channel.basicAck(deliveryTag, false);
			log.info("[결제 처리 완료] meetingId: {}, userId: {}",
				event.meetingId(), event.userId());

		} catch (Exception ex) {
			log.error("[결제 처리 실패] meetingId={}, userId={}, error={}",
				event.meetingId(), event.userId(), ex.getMessage());
			handleMessageFailure(channel, deliveryTag, event.meetingId(), event.userId());
		}
	}

	/**
	 * 2. 참가자 취소 이벤트 처리 - 개별 환불
	 * Queue: payment.participant.canceled.queue
	 * Event: ParticipantEvents.CancelRefund (meetingId, userId, hostUserId, participantNickname)
	 *
	 * Meeting에서 참가 취소 시 발행하는 이벤트
	 * 환불 처리 후 payment.refunded 이벤트 발행
	 */
	@RabbitListener(
		queues = PAYMENT_PARTICIPANT_CANCEL,
		containerFactory = "paymentListenerContainerFactory"
	)
	public void handleParticipantCancelRefund(ParticipantEvents.CancelRefund event,
		Channel channel,
		Message message) {
		long deliveryTag = message.getMessageProperties().getDeliveryTag();

		log.info("[환불 시작] 참가자 취소 이벤트 수신 - meetingId: {}, userId: {}, nickname: {}",
			event.meetingId(), event.userId(), event.participantNickname());

		try {
			// 완료된 결제 조회
			Payment payment = paymentRepository.findByMeetingIdAndUserIdAndStatus(
					event.meetingId(), event.userId(), PaymentStatus.COMPLETED)
				.orElse(null);

			if (payment == null) {
				log.warn("환불할 결제 내역 없음 - meetingId: {}, userId: {}",
					event.meetingId(), event.userId());
				channel.basicAck(deliveryTag, false);
				return;
			}

			// 환불 처리
			// refundPayment 내부에서:
			// 1. 토스 API 호출하여 환불
			// 2. payment.refunded 이벤트 발행
			// 3. 결제 레코드 삭제 (재결제 가능하도록)
			RefundRequestDto refundRequest = new RefundRequestDto(
				String.format("사용자 %s님의 참가 취소", event.participantNickname())
			);

			paymentService.refundPayment(payment.getId(), event.userId(), refundRequest);

			channel.basicAck(deliveryTag, false);
			log.info("[환불 완료] paymentId: {}, meetingId: {}, userId: {}, amount: {}원",
				payment.getId(), event.meetingId(), event.userId(), payment.getAmount());

		} catch (Exception ex) {
			log.error("[환불 실패] meetingId={}, userId={}, error={}",
				event.meetingId(), event.userId(), ex.getMessage());
			handleMessageFailure(channel, deliveryTag, event.meetingId(), event.userId());
		}
	}

	/**
	 * 3. 모임 삭제 이벤트 처리 - 전체 참가자 환불
	 * Queue: payment.meeting.deleted.queue
	 * Event: MeetingMessageEvents.Delete (meetingId, meetingName, userIdList)
	 *
	 * Meeting 삭제 시 발행하는 이벤트
	 * 모든 참가자의 결제를 환불 처리
	 */
	@RabbitListener(
		queues = PAYMENT_MEETING_DELETED,
		containerFactory = "paymentListenerContainerFactory"
	)
	public void handleMeetingDeleted(MeetingMessageEvents.Delete event,
		Channel channel,
		Message message) {
		long deliveryTag = message.getMessageProperties().getDeliveryTag();

		log.info("[모임 삭제 환불 시작] meetingId: {}, meetingName: {}, 참가자 수: {}명",
			event.meetingId(), event.meetingName(),
			event.userIdList() != null ? event.userIdList().size() : 0);

		try {
			// 해당 모임의 모든 완료된 결제 조회
			List<Payment> completedPayments = paymentRepository
				.findByMeetingIdAndStatus(event.meetingId(), PaymentStatus.COMPLETED);

			if (completedPayments.isEmpty()) {
				log.info("환불할 결제 내역 없음 - meetingId: {}", event.meetingId());
				channel.basicAck(deliveryTag, false);
				return;
			}

			log.info("환불 대상: {}건, meetingId: {}",
				completedPayments.size(), event.meetingId());

			// 환불 처리 결과 추적
			int successCount = 0;
			int failCount = 0;

			for (Payment payment : completedPayments) {
				try {
					// 개별 환불 처리
					RefundRequestDto refundRequest = new RefundRequestDto(
						String.format("모임 '%s' 삭제로 인한 자동 환불", event.meetingName())
					);

					paymentService.refundPayment(
						payment.getId(),
						payment.getUserId(),
						refundRequest
					);

					successCount++;
					log.info("[환불 성공] paymentId: {}, userId: {}, amount: {}원",
						payment.getId(), payment.getUserId(), payment.getAmount());

				} catch (Exception refundEx) {
					failCount++;
					log.error("[환불 실패] paymentId: {}, userId: {}, error: {}",
						payment.getId(), payment.getUserId(), refundEx.getMessage());

					// 실패 건은 별도 처리 필요 (수동 환불 대상)
					recordRefundFailure(payment, event.meetingId(), refundEx.getMessage());
				}
			}

			log.info("[모임 삭제 환불 완료] meetingId: {}, 성공: {}건, 실패: {}건",
				event.meetingId(), successCount, failCount);

			// 일부 실패가 있어도 메시지는 ACK (실패 건은 별도 관리)
			channel.basicAck(deliveryTag, false);

		} catch (Exception ex) {
			log.error("[모임 삭제 환불 처리 중 예외] meetingId: {}, error: {}",
				event.meetingId(), ex.getMessage());
			handleMessageFailure(channel, deliveryTag, event.meetingId(), null);
		}
	}

	/**
	 * 메시지 처리 실패 시 공통 처리
	 * DLQ로 전송하여 나중에 재처리 가능하도록 함
	 */
	private void handleMessageFailure(Channel channel, long deliveryTag,
		Long meetingId, Long userId) {
		try {
			// DLQ로 전송 (requeue=false)
			channel.basicNack(deliveryTag, false, false);
			log.warn("메시지를 DLQ로 전송 - meetingId: {}, userId: {}", meetingId, userId);

		} catch (Exception e) {
			try {
				// DLQ 전송도 실패하면 재큐잉
				channel.basicNack(deliveryTag, false, true);
				log.error("DLQ 전송 실패, 재큐잉 - meetingId: {}, userId: {}", meetingId, userId);

			} catch (IOException io) {
				log.error("메시지 NACK 실패 - meetingId: {}, userId: {}", meetingId, userId, io);
			}
		}
	}

	/**
	 * 환불 실패 기록
	 * TODO: 별도 테이블에 저장하여 관리자가 수동으로 처리할 수 있도록 함
	 */
	private void recordRefundFailure(Payment payment, Long meetingId, String errorMessage) {
		// TODO: RefundFailureRecord 엔티티 생성 및 저장
		// TODO: 관리자 알림 발송
		log.error("[환불 실패 기록] paymentId: {}, meetingId: {}, userId: {}, error: {}",
			payment.getId(), meetingId, payment.getUserId(), errorMessage);
	}
}