package com.example.momo.domain.payment.event.rabbitmq.producer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.momo.domain.payment.application.PaymentOutboxService;
import com.example.momo.domain.payment.domain.PaymentOutbox;
import com.example.momo.domain.payment.domain.PaymentOutboxRepository;
import com.example.momo.domain.payment.enums.OutboxStatus;
import com.example.momo.domain.payment.event.rabbitmq.config.PaymentExchangeConfig;
import com.example.momo.domain.payment.event.rabbitmq.dto.PaymentEventDto;
import com.example.momo.global.rabbitmq.dto.PaymentEventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

	@Qualifier("paymentRabbitTemplate")
	private final RabbitTemplate rabbitTemplate;
	private final PaymentOutboxRepository outboxRepository;
	private final ObjectMapper objectMapper;
	private final PaymentOutboxService outboxService;

	/**
	 * 결제 완료 이벤트 처리
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePaymentCompleted(PaymentEventMessage.Completed event) {
		log.info("결제 완료 이벤트 처리 시작 - paymentId: {}, outboxId: {}",
			event.getPaymentId(), event.getOutboxId());

		publishOutboxEvent(event.getOutboxId());
	}

	/**
	 * 결제 실패 이벤트 처리
	 * 즉시 자리 복원
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePaymentFailed(PaymentEventMessage.Failed event) {
		log.info("결제 실패 이벤트 처리 시작 - paymentId: {}, outboxId: {}",
			event.getPaymentId(), event.getOutboxId());

		publishOutboxEvent(event.getOutboxId());
	}

	/**
	 * 환불 이벤트 처리
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePaymentRefunded(PaymentEventMessage.Refunded event) {
		log.info("환불 이벤트 처리 시작 - paymentId: {}, outboxId: {}",
			event.getPaymentId(), event.getOutboxId());

		publishOutboxEvent(event.getOutboxId());
	}

	/**
	 * Outbox 이벤트를 RabbitMQ로 발행
	 */
	public void publishOutboxEvent(Long outboxId) {
		try {
			PaymentOutbox outbox = outboxRepository.findById(outboxId)
				.orElseThrow(() -> new IllegalArgumentException("Outbox not found: " + outboxId));

			// 이미 발행된 경우 스킵
			if (outbox.getStatus() == OutboxStatus.PUBLISHED) {
				log.info("이미 발행된 이벤트 - outboxId: {}", outboxId);
				return;
			}

			PaymentEventDto event = objectMapper.readValue(
				outbox.getPayload(), PaymentEventDto.class);

			// CorrelationData 생성
			CorrelationData correlationData = new CorrelationData(outbox.getCorrelationId());

			// 메시지 발행
			rabbitTemplate.convertAndSend(
				PaymentExchangeConfig.X_PAYMENT_EVENTS,
				outbox.getRoutingKey(),
				event,
				message -> {
					message.getMessageProperties()
						.setHeader("x-outbox-id", outbox.getId());
					return message;
				},
				correlationData
			);

			// Publisher Confirm 대기 (최대 3초)
			boolean isAck = correlationData.getFuture()
				.get(3, TimeUnit.SECONDS).isAck();

			// 성공 처리
			if (isAck && correlationData.getReturned() == null) {
				outboxService.markEventAsPublished(outboxId);
				log.info("이벤트 발행 성공 - outboxId: {}, type: {}",
					outbox.getId(), outbox.getEventType());
			} else {
				String reason = correlationData.getReturned() != null
					? "라우팅 실패" : "브로커 NACK";
				outboxService.markEventAsFailed(outboxId, reason);
				log.error("이벤트 발행 실패 - outboxId: {}, reason: {}",
					outbox.getId(), reason);
			}

		} catch (TimeoutException e) {
			outboxService.markEventAsFailed(outboxId, "Publisher Confirm 타임아웃");
			log.error("Publisher Confirm 타임아웃 - outboxId: {}", outboxId);

		} catch (Exception e) {
			outboxService.markEventAsFailed(outboxId, "발행 중 예외: " + e.getMessage());
			log.error("이벤트 발행 예외 - outboxId: {}", outboxId, e);
		}
	}
}

