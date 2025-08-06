package com.example.momo.global.rabbitmq.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class PaymentEventMessage {

	@Getter
	@RequiredArgsConstructor
	public static class Completed {
		private final Long paymentId;
		private final Long userId;
		private final Long meetingId;
		private final Integer amount;
		private final Long outboxId;
	}

	@Getter
	@RequiredArgsConstructor
	public static class Failed {
		private final Long paymentId;
		private final Long userId;
		private final Long meetingId;
		private final String failReason;
		private final Long outboxId;
	}

	@Getter
	@RequiredArgsConstructor
	public static class Refunded {
		private final Long paymentId;
		private final Long userId;
		private final Long meetingId;
		private final Integer amount;
		private final Long outboxId;
	}
}