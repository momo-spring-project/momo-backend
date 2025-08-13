package com.example.momo.domain.payment.event.rabbitmq.producer;

import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.example.momo.domain.payment.application.PaymentOutboxService;
import com.example.momo.domain.payment.domain.PaymentOutbox;
import com.example.momo.domain.payment.domain.PaymentOutboxRepository;
import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;
import com.example.momo.global.rabbitmq.constant.RoutingKeys;
import com.example.momo.global.rabbitmq.dto.common.EventWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Payment 이벤트 RabbitMQ Producer
 * - 단건 선점(tryMarkProcessing) -> 발행 -> Confirm/Return 콜백에서 최종 상태 마킹
 * - CorrelationData.id = outboxId
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
		ObjectMapper objectMapper
	) {
		this.paymentRabbitTemplate = paymentRabbitTemplate;
		this.outboxRepository = outboxRepository;
		this.outboxService = outboxService;
		this.objectMapper = objectMapper;
	}

	/** Confirm/Returns 콜백 등록 */
	@PostConstruct
	public void setupCallbacks() {
		// Publisher Confirm: 브로커 수신/거부
		paymentRabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
			if (correlationData == null) {
				log.warn("[Payment] ConfirmCallback - correlationData is null");
				return;
			}
			Long outboxId = Long.valueOf(correlationData.getId());
			if (outboxId == null) {
				log.warn("[Payment] ConfirmCallback - invalid correlation id: {}", correlationData.getId());
				return;
			}

			if (ack) {
				// Returns(=unroutable) 있었는지 확인
				if (correlationData.getReturned() != null) {
					handleRoutingFailure(outboxId, correlationData.getReturned());
				} else {
					log.info("[Payment] 발행 성공 - outboxId={}", outboxId);
					outboxService.markEventAsPublished(outboxId);
				}
			} else {
				log.error("[Payment] Publisher NACK - outboxId={}, cause={}", outboxId, cause);
				outboxService.markEventAsFailed(outboxId, "브로커 NACK: " + cause);
			}
		});

		// Publisher Returns: exchange -> 큐 라우팅 실패
		paymentRabbitTemplate.setReturnsCallback(returned -> {
			Long outboxId = Long.valueOf(
				returned.getMessage().getMessageProperties().getMessageId()
			);
			if (outboxId == null) {
				log.warn("[Payment] ReturnsCallback - cannot parse messageId to outboxId");
				return;
			}
			handleRoutingFailure(outboxId, returned);
		});
	}

	/** Outbox 이벤트 발행 (단건 선점 -> 발행) */
	public void publishOutboxEvent(Long outboxId) {
		try {
			// 1) 원자적 선점 (PENDING/FAILED -> PROCESSING)
			if (!outboxService.tryMarkProcessing(outboxId)) {
				log.debug("[Payment] 다른 워커가 선점 - outboxId={}", outboxId);
				return;
			}

			// 2) 조회
			PaymentOutbox outbox = outboxRepository.findById(outboxId)
				.orElseThrow(() -> new IllegalArgumentException("Outbox not found: " + outboxId));

			// 3) 역직렬화
			EventWrapper<?> wrapper = objectMapper.readValue(
				outbox.getPayload(), new TypeReference<EventWrapper<?>>() {
				}
			);

			// 4) CorrelationData: outboxId를 문자열로 사용(추적 단순화)
			final String corrId = String.valueOf(outboxId);
			final CorrelationData correlationData = new CorrelationData(corrId);

			// 5) 발행 (메시지 메타 포함)
			paymentRabbitTemplate.convertAndSend(
				RabbitExchangeNames.PAYMENT_EVENTS,
				outbox.getRoutingKey(),
				wrapper,
				message -> {
					message.getMessageProperties().setMessageId(corrId);             // Confirm/Return에서 outboxId 매칭
					message.getMessageProperties().setHeader("x-outbox-id", outbox.getId());
					message.getMessageProperties().setHeader("x-correlation-id", wrapper.uuId());
					return message;
				},
				correlationData
			);

			log.debug("[Payment] 메시지 발행 요청 완료 - outboxId={}", outboxId);

		} catch (Exception e) {
			log.error("[Payment] 이벤트 발행 실패 - outboxId={}", outboxId, e);
			outboxService.markEventAsFailed(outboxId, e.getMessage());
		}
	}

	/** 라우팅 실패 처리 */
	private void handleRoutingFailure(Long outboxId, ReturnedMessage returned) {
		String routingKey = returned.getRoutingKey();

		if (RoutingKeys.PAYMENT_REFUNDED_KEY.equals(routingKey)) {
			log.info("[Payment] Refund unroutable(정책상 정상) - outboxId={}", outboxId);
			outboxService.markEventAsPublished(outboxId);
		} else {
			log.warn("[Payment] 라우팅 실패 - outboxId={}, key={}, text={}",
				outboxId, routingKey, returned.getReplyText());
			outboxService.markEventAsFailed(outboxId, "라우팅 실패: " + returned.getReplyText());
		}
	}

}

