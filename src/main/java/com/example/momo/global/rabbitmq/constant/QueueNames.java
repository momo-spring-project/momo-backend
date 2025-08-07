package com.example.momo.global.rabbitmq.constant;

public class QueueNames {

	//Message Hub 관련 queue
	public static final String MESSAGE_HUB_QUEUE = "message-hub.queue";
	public static final String MESSAGE_HUB_QUEUE_DLQ = "message-hub.queue.dlq";
	public static final String MESSAGE_HUB_QUEUE_RETRY = "message-hub.queue.retry";

	//Notification 관련 queue
	public static final String NOTIFICATION_QUEUE = "notification.queue";
	public static final String NOTIFICATION_QUEUE_DLQ = "notification.queue.dlq";
	public static final String NOTIFICATION_QUEUE_RETRY = "notification.queue.retry";

	public static final String PAYMENT_PARTICIPANT_REGISTER = "payment.participant.registered.queue";
	public static final String NOTIFICATION_PARTICIPANT_JOIN = "notification.participant.joined.queue";
	public static final String PAYMENT_PARTICIPANT_CANCEL = "payment.participant.canceled.queue";
	public static final String NOTIFICATION_PARTICIPANT_CANCEL = "notification.participant.canceled.queue";

	public static final String PARTICIPANT_PAYMENT_SUCCESS = "participant.payment.success.queue";
	public static final String PARTICIPANT_PAYMENT_FAIL = "participant.payment.fail.queue";
	public static final String DLQ_PARTICIPANT = "participant.dlq";


	public static final String PAYMENT_DLQ = "payment.dlq.queue";
}
