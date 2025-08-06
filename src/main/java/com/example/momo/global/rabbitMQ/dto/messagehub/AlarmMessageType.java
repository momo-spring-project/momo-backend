package com.example.momo.global.rabbitMQ.dto.messagehub;

public final class AlarmMessageType {

	public static final String EVENT_TYPE = "eventType";

	public static final String MEETING_CREATE = "MeetingCreate";
	public static final String MEETING_UPDATE = "MeetingUpdate";
	public static final String MEETING_DELETE = "MeetingDelete";
	public static final String MEETING_JOIN = "MeetingJoin";
	public static final String MEETING_CANCEL = "MeetingCancel";

	public static final String PAID = "PaymentPaid";
	public static final String REFUNDED = "Refunded";
	public static final String FOLLOWED = "Followed";

	private AlarmMessageType() {
		// no-op
	}
}