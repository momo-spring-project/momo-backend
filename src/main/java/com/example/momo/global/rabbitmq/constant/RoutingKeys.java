package com.example.momo.global.rabbitmq.constant;

public class RoutingKeys {
	// User 관련 routing key들
	public static final String USER_WITHDRAWN = "user.withdrawn";
	// Meeting
	public static final String PARTICIPANT_REGISTER = "participant.registered";
	public static final String PARTICIPANT_JOIN = "participant.joined";
	public static final String PARTICIPANT_CANCEL_NOTIFICATION = "participant.canceled.notification";
	public static final String PARTICIPANT_CANCEL_REFUND = "participant.canceled.refund";

	// Payment 도메인 이벤트
	public static final String PAYMENT_COMPLETED = "payment.completed";
	public static final String PAYMENT_FAILED = "payment.failed";
	public static final String PAYMENT_REFUNDED = "payment.refunded";

	public static final String PAYMENT_DLQ = "payment.dlq";

}
