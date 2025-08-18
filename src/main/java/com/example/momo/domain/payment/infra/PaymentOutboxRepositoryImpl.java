package com.example.momo.domain.payment.infra;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.momo.domain.payment.domain.PaymentOutbox;
import com.example.momo.domain.payment.domain.PaymentOutboxRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PaymentOutboxRepositoryImpl implements PaymentOutboxRepository {

	private final PaymentOutboxJpaRepository jpaRepository;

	@Override
	public List<Long> pickPendingIds(int limit) {
		return jpaRepository.pickPendingIds(limit);
	}

	@Override
	public List<Long> pickRetryableFailedIds(int maxRetry, int limit) {
		return jpaRepository.pickRetryableFailedIds(maxRetry, limit);
	}

	@Override
	public int tryMarkProcessing(Long id, LocalDateTime now) {
		return jpaRepository.tryMarkProcessing(id, now);
	}

	@Override
	public int deletePublishedBefore(LocalDateTime threshold) {
		return jpaRepository.deletePublishedBefore(threshold);
	}

	@Override
	public int deleteDlqMessagesBefore(LocalDateTime threshold) {
		return jpaRepository.deleteDlqMessagesBefore(threshold);
	}

	@Override
	public int recoverStuckProcessingEvents(int timeoutMinutes) {
		return jpaRepository.recoverStuckProcessingEvents(timeoutMinutes);
	}

	@Override
	public PaymentOutbox save(PaymentOutbox outbox) {
		return jpaRepository.save(outbox);
	}

	@Override
	public Optional<PaymentOutbox> findById(Long id) {
		return jpaRepository.findById(id);
	}
}
