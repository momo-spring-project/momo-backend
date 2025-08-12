package com.example.momo.domain.payment.event.springEvent;

import java.time.LocalDateTime;

/**
 * Payment 도메인 내부 Spring 이벤트
 * 트랜잭션 내에서 발생하는 이벤트
 */
public class PaymentEvents {

	/**
	 * 결제 완료 내부 이벤트
	 */
	public record Completed(
		Long paymentId,
		Long userId,
		Long meetingId,
		Integer amount,
		String orderId,
		Long outboxId,
		LocalDateTime completedAt
	) {
		public Completed(Long paymentId, Long userId, Long meetingId,
			Integer amount, String orderId, Long outboxId) {
			this(paymentId, userId, meetingId, amount, orderId, outboxId, LocalDateTime.now());
		}
	}

	/**
	 * 결제 실패 내부 이벤트
	 */
	public record Failed(
		Long paymentId,
		Long userId,
		Long meetingId,
		String failReason,
		Long outboxId,
		LocalDateTime failedAt
	) {
		public Failed(Long paymentId, Long userId, Long meetingId, String failReason, Long outboxId) {
			this(paymentId, userId, meetingId, failReason, outboxId, LocalDateTime.now());
		}
	}

	/**
	 * 환불 완료 내부 이벤트
	 */
	public record Refunded(
		Long paymentId,
		Long userId,
		Long meetingId,
		Integer amount,
		String refundReason,
		Long outboxId,
		LocalDateTime refundedAt
	) {
		public Refunded(Long paymentId, Long userId, Long meetingId,
			Integer amount, String reason, Long outboxId) {
			this(paymentId, userId, meetingId, amount, reason, outboxId, LocalDateTime.now());
		}
	}
}