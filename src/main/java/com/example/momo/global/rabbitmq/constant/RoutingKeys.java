package com.example.momo.global.rabbitmq.constant;

public class RoutingKeys {
	// User 관련 routing key들
	public static final String USER_WITHDRAWN_KEY = "user.withdrawn.key";
	public static final String USER_FOLLOWED_KEY = "user.followed.key";

	// Message Hub 관련 Key
	public static final String MESSAGE_HUB_ASSEMBLE_KEY = "message-hub.assemble.key";
	public static final String MESSAGE_HUB_ASSEMBLE_RETRY_KEY = "message-hub.assemble.retry.key";
	public static final String MESSAGE_HUB_ASSEMBLE_DLQ_KEY = "message-hub.assemble.dlq.key";

	//Notification 관련 key
	public static final String NOTIFICATION_SENT_KEY = "notification.sent.key";
	public static final String NOTIFICATION_SENT_DLQ_KEY = "notification.sent.dlq.key";
	public static final String NOTIFICATION_SENT_RETRY_KEY = "notification.sent.retry.key";

	// Meeting
	public static final String PARTICIPANT_REGISTER_KEY = "participant.registered.key";
	public static final String PARTICIPANT_JOIN_KEY = "participant.joined.key";
	public static final String PARTICIPANT_CANCEL_KEY = "participant.canceled.key";
	public static final String PARTICIPANT_DLQ_KEY = "participant.dlq";

	public static final String MEETING_CREATE_KEY = "meeting.created.key";
	public static final String MEETING_UPDATE_KEY = "meeting.updated.key";
	public static final String MEETING_DELETE_KEY = "meeting.deleted.key";

	// Payment 관련 key
	public static final String PAYMENT_COMPLETED_KEY = "payment.completed.key";
	public static final String PAYMENT_FAILED_KEY = "payment.failed.key";
	public static final String PAYMENT_REFUNDED_KEY = "payment.refunded.key";
	public static final String PAYMENT_DLQ = "payment.dlq";
}