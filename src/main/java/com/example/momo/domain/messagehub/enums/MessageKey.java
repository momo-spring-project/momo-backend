package com.example.momo.domain.messagehub.enums;

/**
 * 도메인별 알림 메시지에 대응하는 메시지 키를 정의한 열거형입니다.
 *
 * <p>
 * 각 키는 메시지 프로퍼티 파일(messages.properties 등)에 정의된
 * 템플릿 문자열과 연결되며, 알림 메시지 생성 시 사용됩니다.
 */
public enum MessageKey {
	// Meeting
	MEETING_CREATED("meeting.created"),
	MEETING_UPDATED("meeting.updated"),
	MEETING_DELETED("meeting.deleted"),
	MEETING_JOINED("meeting.joined"),
	MEETING_CANCELED("meeting.canceled"),

	// Follow
	FOLLOWED("follow.followed"),

	// Payment
	PAID("payment.paid"),
	REFUNDED("payment.refunded");

	private final String key;

	MessageKey(String key) {
		this.key = key;
	}

	public String key() {
		return key;
	}
}
