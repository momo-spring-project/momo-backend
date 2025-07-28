package com.example.momo.domain.messagehub.enums;

/**
 * 알림 이벤트의 유형을 나타내는 열거형입니다.
 *
 * <p>
 * 도메인별(모임, 팔로우, 결제 등)로 발생하는 알림의 종류를 구분하며,
 * 클라이언트에 전달되는 알림 타입 식별자 또는 처리 분기 등에 사용됩니다.
 */
public enum NotificationEventType {
	MEETING_JOINED,
	MEETING_CANCELLED,
	MEETING_UPDATED,
	MEETING_DELETED,
	MEETING_RECOMMENDED,

	// Follow
	FOLLOWED,

	// Payment
	PAID,
	REFUNDED
}
