package com.example.momo.global.rabbitmq.constant;

public class RabbitExchangeNames {
	// User 관련 exchange
	public static final String USER_EVENTS = "momo.user.events";

	//Message Hub 관련 exchange
	public static final String MESSAGE_HUB_EVENTS = "momo.message-hub.events";
	public static final String MESSAGE_HUB_EVENTS_DLX = "momo.message-hub.events.dlx";
	public static final String MESSAGE_HUB_EVENTS_RETRY = "momo.message-hub.events.retry";

	//Notification 관련 exchange
	public static final String NOTIFICATION_EVENTS = "momo.notification.events";
	public static final String NOTIFICATION_EVENTS_DLX = "momo.notification.events.dlx";
	public static final String NOTIFICATION_EVENTS_RETRY = "momo.notification.events.retry";

	// Meeting
	public static final String MEETING_EVENTS = "momo.meeting.events";
	public static final String PARTICIPANT_EVENTS = "momo.participant.events";

}

