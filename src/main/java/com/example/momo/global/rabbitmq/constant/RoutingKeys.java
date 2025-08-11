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
	public static final String PARTICIPANT_REGISTER_KEY = "participant.registered";
	public static final String PARTICIPANT_JOIN_KEY = "participant.joined";
	public static final String PARTICIPANT_CANCEL_KEY = "participant.canceled";
	public static final String PARTICIPANT_DLQ_KEY = "participant.canceled";

	public static final String MEETING_CREATE_KEY = "meeting.created.key";
	public static final String MEETING_UPDATE_KEY = "meeting.updated.key";
	public static final String MEETING_DELETE_KEY = "meeting.deleted.key";

	// Payment 관련 key
	public static final String PAYMENT_COMPLETED_KEY = "payment.completed.key";
	public static final String PAYMENT_FAILED_KEY = "payment.failed.key";
	public static final String PAYMENT_REFUNDED_KEY = "payment.refunded.key";
	public static final String PAYMENT_DLQ = "payment.dlq";

}
