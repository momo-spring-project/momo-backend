package com.example.momo.domain.payment.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentOutboxRepository {

	PaymentOutbox save(PaymentOutbox outbox);

	Optional<PaymentOutbox> findById(Long id);

	// 스케줄러용(선점 없음)
	List<Long> pickPendingIds(int limit);

	List<Long> pickRetryableFailedIds(int maxRetry, int limit);

	// 단건 선점(원자적)
	int tryMarkProcessing(Long id, LocalDateTime now);

	int deletePublishedBefore(LocalDateTime threshold);

	int deleteDlqMessagesBefore(LocalDateTime threshold);

	int recoverStuckProcessingEvents(int timeoutMinutes);
}
