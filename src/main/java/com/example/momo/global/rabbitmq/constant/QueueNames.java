package com.example.momo.global.rabbitMQ.constant;

public class QueueNames {

	//Message Hub 관련 queue
	public static final String MESSAGE_HUB_QUEUE = "message-hub.queue";
	public static final String MESSAGE_HUB_QUEUE_DLQ = "message-hub.queue.dlq";
	public static final String MESSAGE_HUB_QUEUE_RETRY = "message-hub.queue.retry";

	//Notification 관련 queue
	public static final String NOTIFICATION_QUEUE = "notification.queue";
	public static final String NOTIFICATION_QUEUE_DLQ = "notification.queue.dlq";
	public static final String NOTIFICATION_QUEUE_RETRY = "notification.queue.retry";
}
