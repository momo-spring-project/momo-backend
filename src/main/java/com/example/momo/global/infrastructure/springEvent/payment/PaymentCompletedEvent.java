package com.example.momo.global.infrastructure.springEvent.payment;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class PaymentCompletedEvent {

	private final Long paymentId;
	private final Long userId;
	private final Long meetingId;
	private final int amount;
	private final LocalDateTime paidAt;

	public PaymentCompletedEvent(Long paymentId, Long userId,
		Long meetingId, int amount, LocalDateTime paidAt) {
		this.paymentId = paymentId;
		this.userId = userId;
		this.meetingId = meetingId;
		this.amount = amount;
		this.paidAt = paidAt;
	}
}