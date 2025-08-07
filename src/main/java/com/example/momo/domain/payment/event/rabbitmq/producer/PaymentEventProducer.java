package com.example.momo.domain.payment.event.rabbitmq.producer;

import java.util.concurrent.TimeUnit;

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
import com.example.momo.domain.payment.event.springEvent.PaymentEventDto;
import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;
import com.example.momo.global.rabbitmq.dto.PaymentEventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentEventProducer {

	private final RabbitTemplate rabbitTemplate;
	private final PaymentOutboxRepository outboxRepository;
	private final ObjectMapper objectMapper;
	private final PaymentOutboxService outboxService;

	// Lombok @RequiredArgsConstructor 대신 명시적 생성자 사용
	public PaymentEventProducer(
		@Qualifier("paymentRabbitTemplate") RabbitTemplate rabbitTemplate,
		PaymentOutboxRepository outboxRepository,
		ObjectMapper objectMapper,
		PaymentOutboxService outboxService) {
		this.rabbitTemplate = rabbitTemplate;
		this.outboxRepository = outboxRepository;
		this.objectMapper = objectMapper;
		this.outboxService = outboxService;
	}

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
	 * Outbox 이벤트를 RabbitMQ로 발행 - 타임아웃 증가 버전
	 *
	 * Spring AMQP 2.x 버전에서는 addCallback이 없으므로
	 * 타임아웃을 늘려서 동기 방식으로 처리
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
				RabbitExchangeNames.PAYMENT_EVENTS,
				outbox.getRoutingKey(),
				event,
				message -> {
					message.getMessageProperties()
						.setHeader("x-outbox-id", outbox.getId());
					return message;
				},
				correlationData
			);

			// Publisher Confirm 대기 - 타임아웃을 10초로 증가
			try {
				CorrelationData.Confirm confirm = correlationData.getFuture()
					.get(10, TimeUnit.SECONDS);

				if (confirm != null && confirm.isAck()) {
					// 성공 처리
					log.info("Publisher Confirm ACK 받음 - outboxId: {}, correlationId: {}",
						outboxId, correlationData.getId());

					// Return 확인 (라우팅 실패 체크)
					if (correlationData.getReturned() != null) {
						log.warn("메시지가 라우팅되지 못함 - outboxId: {}, returned: {}",
							outboxId, correlationData.getReturned());
						outboxService.markEventAsFailed(outboxId, "라우팅 실패");
					} else {
						outboxService.markEventAsPublished(outboxId);
						log.info("이벤트 발행 성공 - outboxId: {}, type: {}",
							outbox.getId(), outbox.getEventType());
					}
				} else {
					// NACK 처리
					String reason = "브로커 NACK";
					log.warn("Publisher Confirm NACK - outboxId: {}, reason: {}",
						outboxId, reason);
					outboxService.markEventAsFailed(outboxId, reason);
				}

			} catch (java.util.concurrent.TimeoutException e) {
				// 타임아웃 처리
				log.error("Publisher Confirm 타임아웃 (10초) - outboxId: {}", outboxId);
				outboxService.markEventAsFailed(outboxId, "Publisher Confirm 타임아웃");

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				log.error("Publisher Confirm 대기 중 인터럽트 - outboxId: {}", outboxId);
				outboxService.markEventAsFailed(outboxId, "인터럽트 발생");

			} catch (Exception e) {
				log.error("Publisher Confirm 처리 중 예외 - outboxId: {}", outboxId, e);
				outboxService.markEventAsFailed(outboxId, "Confirm 처리 실패: " + e.getMessage());
			}

		} catch (Exception e) {
			log.error("이벤트 발행 중 예외 발생 - outboxId: {}", outboxId, e);
			outboxService.markEventAsFailed(outboxId, "발행 중 예외: " + e.getMessage());
		}
	}
}