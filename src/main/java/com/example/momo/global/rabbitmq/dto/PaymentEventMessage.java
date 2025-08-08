package com.example.momo.global.rabbitmq.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface PaymentEventMessage {

	@Getter
	@RequiredArgsConstructor
	class Completed implements PaymentEventMessage {
		private final Long paymentId;
		private final Long userId;
		private final Long meetingId;
		private final Integer amount;
		private final Long outboxId;
	}

	@Getter
	@RequiredArgsConstructor
	class Failed implements PaymentEventMessage {
		private final Long paymentId;
		private final Long userId;
		private final Long meetingId;
		private final String failReason;
		private final Long outboxId;
	}

	@Getter
	@RequiredArgsConstructor
	class Refunded implements PaymentEventMessage {
		private final Long paymentId;
		private final Long userId;
		private final Long meetingId;
		private final Integer amount;
		private final Long outboxId;
	}
}