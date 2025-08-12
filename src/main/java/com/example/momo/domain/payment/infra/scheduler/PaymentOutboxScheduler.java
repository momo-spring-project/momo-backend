package com.example.momo.domain.payment.infra.scheduler;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.momo.domain.payment.domain.PaymentOutbox;
import com.example.momo.domain.payment.domain.PaymentOutboxRepository;
import com.example.momo.domain.payment.enums.OutboxStatus;
import com.example.momo.domain.payment.event.rabbitmq.producer.PaymentEventProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Outbox 테이블 기반 결제 이벤트 발행 스케줄러
 *
 * - PENDING 상태의 미발행 이벤트 재시도
 * - FAILED 상태의 실패 이벤트 최대 3회까지 재시도
 * - 3회 이상 실패 시 논리적 DLQ 처리 (status = DEAD_LETTERED)
 *
 * RabbitMQ의 물리적 DLQ가 아닌 DB 내 상태 필드를 이용한 논리적 DLQ 방식
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutboxScheduler {

	private final PaymentOutboxRepository outboxRepository;
	private final PaymentEventProducer eventProducer;

	/**
	 * 10초마다 실행
	 * - PENDING → 초기 발행 실패 이벤트 재시도
	 * - FAILED → 3회 미만 실패 이벤트 재시도
	 * - 3회 이상 실패한 이벤트는 DEAD_LETTERED 상태로 전환
	 */
	@Scheduled(fixedDelay = 1000000000)
	public void publishUnsentMessages() {
		// PENDING 상태 메시지 (초기 발행 실패)
		List<PaymentOutbox> pendingEvents = outboxRepository
			.findByStatusOrderByCreatedAt(OutboxStatus.PENDING, PageRequest.of(0, 100));

		for (PaymentOutbox outbox : pendingEvents) {
			try {
				log.info("미발행 이벤트 재시도 - outboxId: {}", outbox.getId());
				eventProducer.publishOutboxEvent(outbox.getId());
			} catch (Exception e) {
				log.error("스케줄러 발행 실패 - outboxId: {}", outbox.getId(), e);
			}
		}

		// FAILED 상태이면서 재시도 가능한 메시지(FAILED && retryCount < 3)
		List<PaymentOutbox> retryableEvents = outboxRepository
			.findByStatusAndRetryCountLessThan(OutboxStatus.FAILED, 3);

		for (PaymentOutbox outbox : retryableEvents) {
			try {
				log.info("실패 이벤트 재시도 - outboxId: {}, retry: {}",
					outbox.getId(), outbox.getRetryCount());
				eventProducer.publishOutboxEvent(outbox.getId());
			} catch (Exception e) {
				log.error("스케줄러 재시도 실패 - outboxId: {}", outbox.getId(), e);
			}
		}

		// 3회 초과 실패 메시지 논리적 dlq 처리
		List<PaymentOutbox> deadLetterCandidates = outboxRepository
			.findByStatusAndRetryCountGreaterThanEqual(OutboxStatus.FAILED, 3);

		for (PaymentOutbox outbox : deadLetterCandidates) {
			outbox.markAsDeadLettered();
			log.warn("Outbox DLQ 처리 - outboxId: {}", outbox.getId());
		}
	}
}