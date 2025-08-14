package com.example.momo.domain.payment.application;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentOutboxService {

	void markEventAsPublished(Long outboxId);

	void markEventAsFailed(Long outboxId, String reason);

	boolean tryMarkProcessing(Long outboxId);

	List<Long> pickPendingIds(int limit);

	List<Long> pickRetryableFailedIds(int maxRetry, int limit);

	void cleanupOldMessages(LocalDateTime now);

	int recoverStuckProcessingEvents(int minutes);
}
