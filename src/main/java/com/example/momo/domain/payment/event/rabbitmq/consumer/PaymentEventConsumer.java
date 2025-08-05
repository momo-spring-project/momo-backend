package com.example.momo.domain.payment.event.rabbitmq.consumer;

import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.payment.domain.Payment;
import com.example.momo.domain.payment.domain.PaymentRepository;
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

	@RabbitListener(
		queues = "payment.participant.created.queue",  // PaymentQueueConfig에 추가 필요
		containerFactory = "paymentListenerContainerFactory"
	)
	@Transactional
	public void handleParticipantCreated(MeetingParticipantEventDto event, Channel channel, Message message) {
		long deliveryTag = message.getMessageProperties().getDeliveryTag();

		log.info("참가자 생성 이벤트 수신 - meetingId: {}, userId: {}, amount: {}",
			event.getMeetingId(), event.getUserId(), event.getAmount());

		try {
			// 이미 결제 레코드가 있는지 확인
			boolean paymentExists = paymentRepository.findByMeetingIdAndUserId(
				event.getMeetingId(), event.getUserId()
			).isPresent();

			if (!paymentExists) {
				// PENDING 결제 생성
				Payment payment = Payment.createPending(
					event.getUserId(),
					event.getMeetingId(),
					event.getAmount()
				);

				paymentRepository.save(payment);

				log.info("PENDING 결제 생성 완료 - paymentId: {}, meetingId: {}, userId: {}",
					payment.getId(), event.getMeetingId(), event.getUserId());
			} else {
				log.warn("이미 결제 레코드가 존재합니다 - meetingId: {}, userId: {}",
					event.getMeetingId(), event.getUserId());
			}

			// 수동 ACK
			channel.basicAck(deliveryTag, false);

		} catch (Exception e) {
			log.error("참가자 생성 이벤트 처리 실패 - meetingId: {}, userId: {}",
				event.getMeetingId(), event.getUserId(), e);

			try {
				// 실패시 DLQ로 (재큐잉 안함)
				channel.basicNack(deliveryTag, false, true);
			} catch (IOException ex) {
				log.error("메시지 NACK 실패", ex);
			}
		}
	}
}