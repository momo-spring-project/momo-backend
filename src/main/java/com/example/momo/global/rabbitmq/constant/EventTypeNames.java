package com.example.momo.global.rabbitmq.constant;

//EventType 명시 예시
public class EventTypeNames {

	public static final String EVENT_TYPE = "eventType";

	public static final String MEETING_CREATE = "meeting.create";
	public static final String MEETING_UPDATE = "meeting.update";
	public static final String MEETING_DELETE = "meeting.delete";
	public static final String MEETING_JOIN = "meeting.join";
	public static final String MEETING_CANCEL = "meeting.cancel";

	public static final String FOLLOWED = "followed";

	// Meeting 이벤트 타입
	public static final String MEETING_PARTICIPANT_REGISTER = "meeting.participant.register";
	public static final String MEETING_PARTICIPANT_JOIN = "meeting.participant.join";
	public static final String MEETING_PARTICIPANT_CANCEL = "meeting.participant.cancel";

	// Payment 이벤트 타입
	public static final String PAYMENT_COMPLETED = "payment.completed";
	public static final String PAYMENT_FAILED = "payment.failed";
	public static final String PAYMENT_REFUNDED = "payment.refunded";
}