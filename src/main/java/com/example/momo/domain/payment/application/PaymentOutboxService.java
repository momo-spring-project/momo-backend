package com.example.momo.domain.payment.application;

public interface PaymentOutboxService {

	void markEventAsPublished(Long outboxId);

	void markEventAsFailed(Long outboxId, String reason);
}
