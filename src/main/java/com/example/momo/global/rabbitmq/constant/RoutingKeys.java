package com.example.momo.global.rabbitmq.constant;

public class RoutingKeys {
	// User 관련 routing key들
	public static final String USER_WITHDRAWN = "user.withdrawn";

	// Message Hub 관련 Key
	public static final String MESSAGE_HUB_ASSEMBLE_KEY = "message-hub.assemble";
	public static final String MESSAGE_HUB_ASSEMBLE_DLX_KEY = "message-hub.assemble.dlx";

	//Notification 관련 key
	public static final String NOTIFICATION_SENT_KEY = "notification.sent";
	public static final String NOTIFICATION_SENT_DLX_KEY = "notification.sent.dlx";
	public static final String NOTIFICATION_SENT_RETRY_KEY = "notification.sent.retry";

	// Meeting
	public static final String PARTICIPANT_REGISTER_KEY = "participant.registered";
	public static final String PARTICIPANT_JOIN_KEY = "participant.joined";
	public static final String PARTICIPANT_CANCEL_KEY = "participant.canceled";
	public static final String PARTICIPANT_DLQ_KEY = "participant.canceled";

	public static final String MEETING_CREATE = "meeting.created";
	public static final String MEETING_UPDATE = "meeting.updated";
	public static final String MEETING_DELETE = "meeting.deleted";

	// Payment 관련 key
	public static final String PAYMENT_COMPLETED_KEY = "payment.completed";
	public static final String PAYMENT_FAILED_KEY = "payment.failed";
	public static final String PAYMENT_REFUNDED_KEY = "payment.refunded";
	public static final String PAYMENT_DLQ = "payment.dlq";

}
