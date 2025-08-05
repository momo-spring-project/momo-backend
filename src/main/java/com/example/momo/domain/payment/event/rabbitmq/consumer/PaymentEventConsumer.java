package com.example.momo.domain.payment.event.rabbitmq.consumer;

import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.momo.domain.payment.application.PaymentService;
import com.example.momo.domain.payment.application.dto.CardPaymentTestRequestDto;
import com.example.momo.domain.payment.domain.PaymentRepository;
import com.example.momo.domain.payment.enums.PaymentStatus;
import com.example.momo.domain.payment.event.rabbitmq.dto.MeetingParticipantEventDto;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Meeting 도메인에서 발행하는 참가자 생성 이벤트를 구독하여
 * PENDING 상태의 결제 레코드를 생성하는 Consumer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

	private final PaymentRepository paymentRepository;
	private final PaymentService paymentService;

	@RabbitListener(
		queues = "payment.participant.created.queue",  // PaymentQueueConfig에 추가 필요
		containerFactory = "paymentListenerContainerFactory"
	)
	public void handleParticipantCreated(MeetingParticipantEventDto event, Channel channel, Message message) {
		long deliveryTag = message.getMessageProperties().getDeliveryTag();

		log.info("참가자 생성 이벤트 수신 - meetingId: {}, userId: {}, amount: {}",
			event.getMeetingId(), event.getUserId(), event.getAmount());

		try {
			// 이미 결제 완료된 경우
			if (paymentRepository.existsByMeetingIdAndUserIdAndStatus(
				event.getMeetingId(), event.getUserId(), PaymentStatus.COMPLETED)) {
				log.warn("이미 결제 완료 – meetingId={}, userId={}",
					event.getMeetingId(), event.getUserId());
				channel.basicAck(deliveryTag, false);
				return;
			}

			//결제 실행 (Outbox + DomainEvent 포함)
			CardPaymentTestRequestDto request = CardPaymentTestRequestDto.builder()
				.meetingId(event.getMeetingId())
				.build();

			paymentService.createTestKeyInPayment(request, event.getUserId());

			// 비즈니스 성공 → ACK
			channel.basicAck(deliveryTag, false);

		} catch (Exception ex) {
			log.error("참가자 결제 처리 실패 – meetingId={}, userId={}",
				event.getMeetingId(), event.getUserId(), ex);

			try {
				// 재시도 3회 후 DLQ로 보내도록 requeue=false
				channel.basicNack(deliveryTag, false, false);
			} catch (Exception e) {
				log.error("참가자 생성 이벤트 처리 실패 - meetingId: {}, userId: {}",
					event.getMeetingId(), event.getUserId(), e);

				try {
					// 실패시 DLQ로 (재큐잉 안함)
					channel.basicNack(deliveryTag, false, true);
				} catch (IOException io) {
					log.error("메시지 NACK 실패", io);
				}
			}
		}
	}
}