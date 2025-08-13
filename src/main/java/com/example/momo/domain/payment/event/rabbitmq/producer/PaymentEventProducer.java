package com.example.momo.domain.payment.event.rabbitmq.producer;

import java.util.concurrent.TimeUnit;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.example.momo.domain.payment.application.PaymentOutboxService;
import com.example.momo.domain.payment.domain.PaymentOutbox;
import com.example.momo.domain.payment.domain.PaymentOutboxRepository;
import com.example.momo.domain.payment.enums.OutboxStatus;
import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;
import com.example.momo.global.rabbitmq.constant.RoutingKeys;
import com.example.momo.global.rabbitmq.dto.common.EventWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Payment 이벤트 RabbitMQ Producer
 * Outbox 패턴과 EventWrapper를 함께 사용
 */
@Slf4j
@Component
public class PaymentEventProducer {

	private final RabbitTemplate paymentRabbitTemplate;
	private final PaymentOutboxRepository outboxRepository;
	private final PaymentOutboxService outboxService;
	private final ObjectMapper objectMapper;

	public PaymentEventProducer(
		@Qualifier("paymentRabbitTemplate") RabbitTemplate paymentRabbitTemplate,
		PaymentOutboxRepository outboxRepository,
		PaymentOutboxService outboxService,
		ObjectMapper objectMapper) {
		this.paymentRabbitTemplate = paymentRabbitTemplate;
		this.outboxRepository = outboxRepository;
		this.outboxService = outboxService;
		this.objectMapper = objectMapper;
	}

	/**
	 * Outbox 이벤트 발행
	 */
	public void publishOutboxEvent(Long outboxId) {
		try {
			PaymentOutbox outbox = outboxRepository.findById(outboxId)
				.orElseThrow(() -> new IllegalArgumentException("Outbox not found: " + outboxId));

			// 이미 발행된 경우 스킵
			if (outbox.getStatus() == OutboxStatus.PUBLISHED) {
				log.info("[Payment] 이미 발행된 이벤트 - outboxId={}", outboxId);
				return;
			}

			// Wrapper로 바로 역직렬화
			EventWrapper<?> wrapper = objectMapper.readValue(
				outbox.getPayload(),
				new TypeReference<EventWrapper<?>>() {
				}
			);

			// CorrelationData 생성
			CorrelationData correlationData = new CorrelationData(outbox.getCorrelationId());

			// 메시지 발행
			paymentRabbitTemplate.convertAndSend(
				RabbitExchangeNames.PAYMENT_EVENTS,
				outbox.getRoutingKey(),
				wrapper,
				message -> {
					message.getMessageProperties()
						.setHeader("x-outbox-id", outbox.getId());
					message.getMessageProperties().
						setHeader("x-correlation-id", wrapper.uuId());
					return message;
				},
				correlationData
			);

			// Publisher Confirm 대기
			try {
				CorrelationData.Confirm confirm = correlationData.getFuture()
					.get(5, TimeUnit.SECONDS);

				boolean isRefund = RoutingKeys.PAYMENT_REFUNDED_KEY.equals(outbox.getRoutingKey());

				if (confirm != null && confirm.isAck()) {
					// 브로커까지는 도착
					if (correlationData.getReturned() != null) {
						// 교환기 -> 큐 라우팅 실패
						if (isRefund) {
							//  환불은 소비자 없음이 정상 -> 성공 처리
							log.info("[Payment] Refund unroutable 정상 outboxId={}", outboxId);
							outboxService.markEventAsPublished(outboxId);
						} else {
							// 그 외 이벤트는 실패 처리
							log.warn("[Payment] Unroutable 실패 outboxId={}", outboxId);
							outboxService.markEventAsFailed(outboxId, "라우팅 실패");
						}
					} else {
						outboxService.markEventAsPublished(outboxId);
						log.info("[Payment] 이벤트 발행 성공 - outboxId={}", outboxId);
					}
				} else {
					log.warn("[Payment] Publisher Confirm NACK - outboxId={}", outboxId);
					outboxService.markEventAsFailed(outboxId, "브로커 NACK");
				}

			} catch (Exception e) {
				log.error("[Payment] Publisher Confirm 처리 실패 - outboxId={}", outboxId, e);
				outboxService.markEventAsFailed(outboxId, e.getMessage());
			}

		} catch (Exception e) {
			log.error("[Payment] 이벤트 발행 실패 - outboxId={}", outboxId, e);
			outboxService.markEventAsFailed(outboxId, e.getMessage());
		}
	}

}
