package com.example.momo.domain.payment.event.springEvent;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.momo.domain.payment.event.rabbitmq.producer.PaymentEventProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Payment 도메인 Spring 이벤트 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventHandler {

	private final PaymentEventProducer paymentEventProducer;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePaymentCompleted(PaymentEvents.Completed event) {

		log.info("[Payment] 결제 완료 이벤트 처리 - paymentId={}", event.paymentId());
		paymentEventProducer.publishOutboxEvent(event.outboxId());
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePaymentFailed(PaymentEvents.Failed event) {

		log.info("[Payment] 결제 실패 이벤트 처리 - paymentId={}", event.paymentId());
		paymentEventProducer.publishOutboxEvent(event.outboxId());
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePaymentRefunded(PaymentEvents.Refunded event) {
		
		log.info("[Payment] 환불 완료 이벤트 처리 - paymentId={}", event.paymentId());
		paymentEventProducer.publishOutboxEvent(event.outboxId());
	}
}
