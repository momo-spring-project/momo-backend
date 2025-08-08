package com.example.momo.global.rabbitmq.constant;

public class RoutingKeys {
	// User 관련 routing key들
	public static final String USER_WITHDRAWN = "user.withdrawn";

	// Message Hub 관련 Key
	public static final String MESSAGE_HUB_ASSEMBLE = "message-hub.assemble";
	public static final String MESSAGE_HUB_ASSEMBLE_DLX = "message-hub.assemble.dlx";
	public static final String MESSAGE_HUB_ASSEMBLE_RETRY = "message-hub.assemble.retry";

	//Notification 관련 key
	public static final String NOTIFICATION_SENT = "notification.sent";
	public static final String NOTIFICATION_SENT_DLX = "notification.sent.dlx";
	public static final String NOTIFICATION_SENT_RETRY = "notification.sent.retry";

	// Meeting
	public static final String PARTICIPANT_REGISTER = "participant.registered";
	public static final String PARTICIPANT_JOIN = "participant.joined";
	public static final String PARTICIPANT_CANCEL = "participant.canceled";

	// Payment 도메인 이벤트
	public static final String PAYMENT_COMPLETED = "payment.completed";
	public static final String PAYMENT_FAILED = "payment.failed";
	public static final String PAYMENT_REFUNDED = "payment.refunded";
	public static final String PAYMENT_DLQ = "payment.dlq";
}
