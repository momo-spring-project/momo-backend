package com.example.momo.global.rabbitMQ.constant;

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
}
