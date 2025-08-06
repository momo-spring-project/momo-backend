package com.example.momo.domain.payment.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.example.momo.domain.payment.enums.OutboxStatus;

public interface PaymentOutboxRepository {

	PaymentOutbox save(PaymentOutbox outbox);

	Optional<PaymentOutbox> findById(Long id);

	int deletePublishedBefore(LocalDateTime threshold);

	int deleteDlqMessagesBefore(LocalDateTime threshold);

	List<PaymentOutbox> findByStatusOrderByCreatedAt(OutboxStatus status, Pageable pageable);

	List<PaymentOutbox> findByStatusAndRetryCountLessThan(OutboxStatus status, int maxRetry);

	List<PaymentOutbox> findByStatusAndRetryCountGreaterThanEqual(OutboxStatus status, int retryCount);
}