package com.example.momo.global.rabbitmq.dto.payment;

import java.time.LocalDateTime;

/**
 * Payment 도메인 이벤트 메시지 정의
 * 도메인 간 이벤트 통신에 사용
 */
public class PaymentEventMessages {

	public interface PaymentEventMessage {
		Long paymentId();

		Long userId();

		Long meetingId();

		LocalDateTime occurredAt();
	}

	public record Completed(
		Long paymentId,
		Long userId,
		Long meetingId,
		Integer amount,
		String orderId,
		Long outboxId,
		LocalDateTime occurredAt
	) implements PaymentEventMessage {
		public Completed(Long paymentId, Long userId, Long meetingId,
			Integer amount, String orderId, Long outboxId) {
			this(paymentId, userId, meetingId, amount, orderId, outboxId, LocalDateTime.now());
		}
	}

	public record Failed(
		Long paymentId,
		Long userId,
		Long meetingId,
		String failReason,
		Long outboxId,
		LocalDateTime occurredAt
	) implements PaymentEventMessage {
		public Failed(Long paymentId, Long userId, Long meetingId,
			String failReason, Long outboxId) {
			this(paymentId, userId, meetingId, failReason, outboxId, LocalDateTime.now());
		}
	}

	public record Refunded(
		Long paymentId,
		Long userId,
		Long meetingId,
		Integer amount,
		String refundReason,
		Long outboxId,
		LocalDateTime occurredAt
	) implements PaymentEventMessage {
		public Refunded(Long paymentId, Long userId, Long meetingId,
			Integer amount, String reason, Long outboxId) {
			this(paymentId, userId, meetingId, amount, reason, outboxId, LocalDateTime.now());
		}
	}
}