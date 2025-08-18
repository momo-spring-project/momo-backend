package com.example.momo.global.rabbitmq.constant;

public class QueueNames {

	//Message Hub 관련 queue
	public static final String MESSAGE_HUB_QUEUE = "message-hub.queue";
	public static final String MESSAGE_HUB_QUEUE_RETRY = "message-hub.queue.retry";
	public static final String MESSAGE_HUB_QUEUE_DLQ = "message-hub.queue.dlq";

	//Notification 관련 queue
	public static final String NOTIFICATION_QUEUE = "notification.queue";
	public static final String NOTIFICATION_QUEUE_RETRY = "notification.queue.retry";

	public static final String PAYMENT_PARTICIPANT_REGISTER = "payment.participant.registered.queue";
	public static final String PAYMENT_PARTICIPANT_CANCEL = "payment.participant.canceled.queue";

	public static final String PARTICIPANT_PAYMENT_SUCCEED = "participant.payment.succeed.queue";
	public static final String PARTICIPANT_PAYMENT_FAILED = "participant.payment.canceled.queue";
	public static final String PARTICIPANT_DLQ = "participant.dlq.queue";
	public static final String PAYMENT_MEETING_DELETED = "payment.meeting.deleted.queue";
	public static final String PAYMENT_DLQ = "payment.dlq.queue";
}
