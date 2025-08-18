package com.example.momo.domain.payment.application.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.momo.domain.payment.domain.PaymentOutboxService;
import com.example.momo.domain.payment.event.rabbitmq.producer.PaymentEventProducer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentOutboxScheduler {

	private static final int BATCH_SIZE = 16;   // 한 사이클에 몇 건을 발행할지
	private static final int MAX_RETRY_COUNT = 3;

	private final PaymentOutboxService outboxService;
	private final PaymentEventProducer eventProducer; // 실제 브로커 발행
	private final Executor outboxPublisherExecutor;   // 병렬 발행

	public PaymentOutboxScheduler(
		PaymentOutboxService outboxService,
		PaymentEventProducer eventProducer,
		@Qualifier("outboxPublisherExecutor") Executor outboxPublisherExecutor
	) {
		this.outboxService = outboxService;
		this.eventProducer = eventProducer;
		this.outboxPublisherExecutor = outboxPublisherExecutor;
	}

	/** PENDING 처리 - 10초 */
	@Scheduled(fixedDelay = 10_000)
	public void publishPendingMessages() {
		List<Long> ids = outboxService.pickPendingIds(BATCH_SIZE); // 선점 없이 ID만
		if (ids.isEmpty())
			return;

		log.info("[PENDING] 디스패치 시작: {}건", ids.size());
		dispatchIdsAsync("PENDING", ids);
	}

	/** FAILED 재시도 - 30초 */
	@Scheduled(fixedDelay = 30_000)
	public void retryFailedMessages() {
		List<Long> ids = outboxService.pickRetryableFailedIds(MAX_RETRY_COUNT, BATCH_SIZE);
		if (ids.isEmpty())
			return;

		log.info("[FAILED] 재시도 디스패치 시작: {}건", ids.size());
		dispatchIdsAsync("FAILED", ids);
	}

	/** 오래된 메시지 정리 - 매일 03:00 */
	@Scheduled(cron = "0 0 3 * * *")
	public void cleanupOldMessages() {
		outboxService.cleanupOldMessages(LocalDateTime.now());
	}

	/** PROCESSING 방치 복구 - 10분 */
	@Scheduled(fixedDelay = 600_000)
	public void recoverStuckProcessingEvents() {
		outboxService.recoverStuckProcessingEvents(5);
	}

	// ===== 내부 헬퍼 =====

	/** 각 ID를 스레드풀에 보내 병렬 발행 (runAsync, 기다리지 않고 즉시 반환) */
	private void dispatchIdsAsync(String tag, List<Long> ids) {
		for (Long id : ids) {
			CompletableFuture.runAsync(() -> safePublish(tag, id), outboxPublisherExecutor);
		}
	}

	/** 예외가 나와도 스케줄러 스레드를 끊지 않도록 방어 */
	private void safePublish(String tag, Long outboxId) {
		try {
			// 단건 선점은 publishOutboxEvent 내부에서 tryMarkProcessing으로 처리함
			eventProducer.publishOutboxEvent(outboxId);
		} catch (Exception e) {
			log.error("[{}] 발행 실패 - outboxId={}", tag, outboxId, e);
			// 예외가 나더라도 Confirm/Return 콜백/서비스에서 실패 마킹 로직이 있으므로 여기선 로깅만
		}
	}
}
