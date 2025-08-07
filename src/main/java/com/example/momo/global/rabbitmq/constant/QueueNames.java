package com.example.momo.global.rabbitmq.constant;

public class QueueNames {
	public static final String PAYMENT_PARTICIPANT_REGISTER = "payment.participant.registered.queue";
	public static final String NOTIFICATION_PARTICIPANT_JOIN = "notification.participant.joined.queue";
	public static final String PAYMENT_PARTICIPANT_CANCEL = "payment.participant.canceled.queue";
	public static final String NOTIFICATION_PARTICIPANT_CANCEL = "notification.participant.canceled.queue";

	public static final String PARTICIPANT_PAYMENT_SUCCESS = "participant.payment.success.queue";
	public static final String PARTICIPANT_PAYMENT_FAIL = "participant.payment.fail.queue";
	public static final String DLQ_PARTICIPANT = "participant.dlq";

}
