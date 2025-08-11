package com.example.momo.global.rabbitmq.constant;

public class RoutingKeys {
	// User 관련 routing key들
	public static final String USER_WITHDRAWN = "user.withdrawn";
	public static final String USER_FOLLOWED = "user.followed";

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
	public static final String PARTICIPANT_CANCEL_NOTIFICATION = "participant.canceled.notification";
	public static final String PARTICIPANT_CANCEL_REFUND = "participant.canceled.refund";

	public static final String MEETING_CREATE = "meeting.created";
	public static final String MEETING_UPDATE = "meeting.updated";
	public static final String MEETING_DELETE = "meeting.deleted";

	// Payment 관련 key
	public static final String PAYMENT_COMPLETED = "payment.completed";
	public static final String PAYMENT_FAILED = "payment.failed";
	public static final String PAYMENT_REFUNDED = "payment.refunded";
	public static final String PAYMENT_DLQ = "payment.dlq";

}
