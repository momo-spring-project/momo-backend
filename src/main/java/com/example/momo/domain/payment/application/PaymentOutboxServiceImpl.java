package com.example.momo.domain.payment.application;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.payment.domain.PaymentOutbox;
import com.example.momo.domain.payment.domain.PaymentOutboxRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentOutboxServiceImpl implements PaymentOutboxService {
	private final PaymentOutboxRepository outboxRepository;

	/**
	 * 발행 성공 처리
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markEventAsPublished(Long outboxId) {
		PaymentOutbox outbox = outboxRepository.findById(outboxId)
			.orElseThrow(() -> new IllegalArgumentException("Outbox not found: " + outboxId));
		outbox.markAsPublished();
	}

	/**
	 * 발행 실패 처리
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markEventAsFailed(Long outboxId, String reason) {
		PaymentOutbox outbox = outboxRepository.findById(outboxId)
			.orElseThrow(() -> new IllegalArgumentException("Outbox not found: " + outboxId));
		outbox.markAsFailed(reason);
	}
}
