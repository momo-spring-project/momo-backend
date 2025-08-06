package com.example.momo.domain.payment.infra;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.payment.domain.PaymentOutbox;
import com.example.momo.domain.payment.domain.PaymentOutboxRepository;
import com.example.momo.domain.payment.enums.OutboxStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PaymentOutboxRepositoryImpl implements PaymentOutboxRepository {

	private final PaymentOutboxJpaRepository jpaRepository;

	@Override
	public PaymentOutbox save(PaymentOutbox outbox) {
		return jpaRepository.save(outbox);
	}

	@Override
	public Optional<PaymentOutbox> findById(Long id) {
		return jpaRepository.findById(id);
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
	public List<PaymentOutbox> findByStatusOrderByCreatedAt(OutboxStatus status, Pageable pageable) {
		return jpaRepository.findByStatusOrderByCreatedAt(status, pageable);
	}

	@Override
	public List<PaymentOutbox> findByStatusAndRetryCountLessThan(OutboxStatus status, int maxRetry) {
		return jpaRepository.findByStatusAndRetryCountLessThan(status, maxRetry);
	}

	@Override
	public List<PaymentOutbox> findByStatusAndRetryCountGreaterThanEqual(OutboxStatus status, int retryCount) {
		return jpaRepository.findByStatusAndRetryCountGreaterThanEqual(status, retryCount);
	}
}