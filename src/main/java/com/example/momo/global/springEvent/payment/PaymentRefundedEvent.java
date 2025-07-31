package com.example.momo.global.springEvent.payment;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class PaymentRefundedEvent {

	private final Long paymentId;
	private final Long userId;
	private final Long meetingId;
	private final int amount;
	private final LocalDateTime refundedAt;

	public PaymentRefundedEvent(Long paymentId, Long userId,
		Long meetingId, int amount, LocalDateTime refundedAt) {
		this.paymentId = paymentId;
		this.userId = userId;
		this.meetingId = meetingId;
		this.amount = amount;
		this.refundedAt = refundedAt;
	}
}
