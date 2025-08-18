package com.example.momo.domain.payment.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.payment.domain.PaymentOutbox;
import com.example.momo.domain.payment.domain.PaymentOutboxRepository;
import com.example.momo.domain.payment.domain.PaymentOutboxService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentOutboxServiceImpl implements PaymentOutboxService {

	private final PaymentOutboxRepository outboxRepository;
	private static final int MAX_RETRY_COUNT = 3;

	/** 발행 성공 처리 */
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markEventAsPublished(Long outboxId) {
		PaymentOutbox outbox = outboxRepository.findById(outboxId)
			.orElseThrow(() -> new IllegalArgumentException("Outbox not found: " + outboxId));
		outbox.markAsPublished();
	}

	/** 발행 실패 처리 (지수 백오프 포함) */
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markEventAsFailed(Long outboxId, String reason) {
		PaymentOutbox outbox = outboxRepository.findById(outboxId)
			.orElseThrow(() -> new IllegalArgumentException("Outbox not found: " + outboxId));
		outbox.markAsFailed(reason);
		if (outbox.getRetryCount() >= MAX_RETRY_COUNT) {
			outbox.markAsDeadLettered();
		}
	}

	/** 단건 PROCESSING 선점 시도 (원자적) */
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean tryMarkProcessing(Long outboxId) {
		return outboxRepository.tryMarkProcessing(outboxId, LocalDateTime.now()) == 1;
	}

	/** 스케줄러: PENDING ID 픽업(선점 없음) */
	@Override
	@Transactional(readOnly = true)
	public List<Long> pickPendingIds(int limit) {
		return outboxRepository.pickPendingIds(limit);
	}

	/** 스케줄러: 재시도 가능 FAILED ID 픽업(선점 없음) */
	@Override
	@Transactional(readOnly = true)
	public List<Long> pickRetryableFailedIds(int maxRetry, int limit) {
		return outboxRepository.pickRetryableFailedIds(maxRetry, limit);
	}

	/** 오래된 메시지 정리 */
	@Override
	@Transactional
	public void cleanupOldMessages(LocalDateTime now) {
		int delPub = outboxRepository.deletePublishedBefore(now.minusDays(30));
		int delDlq = outboxRepository.deleteDlqMessagesBefore(now.minusDays(90));
		log.info("정리 완료 - PUBLISHED: {} DLQ: {}", delPub, delDlq);
	}

	/** PROCESSING 방치 복구 */
	@Override
	@Transactional
	public int recoverStuckProcessingEvents(int minutes) {
		return outboxRepository.recoverStuckProcessingEvents(minutes);
	}
}
