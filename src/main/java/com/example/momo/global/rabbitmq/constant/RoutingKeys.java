package com.example.momo.global.rabbitmq.constant;

public class RoutingKeys {
	// User 관련 routing key들
	public static final String USER_WITHDRAWN = "user.withdrawn";
	// Meeting
	public static final String PARTICIPANT_REGISTER = "participant.registered";
	public static final String PARTICIPANT_JOIN = "participant.joined";
	public static final String PARTICIPANT_CANCEL = "participant.canceled";
}
