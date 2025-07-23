package com.example.momo.domain.notification.enums;

public enum NotificationType {
	MEETING_REQUESTED("참여 신청"),
	MEETING_CANCELLED("참여 취소"),

	MEETING_SOON("모임 임박"),
	MEETING_TODAY("오늘 모임"),
	MEETING_ENDED("모임 종료"),

	MEETING_UPDATED("모임 수정"),
	MEETING_DELETED("모임 삭제"),

	MEETING_RECOMMENDED("모임 추천"),
	MEETING_HOST_NOTICE("모임 공지"),
	SYSTEM_NOTICE("시스템 공지"),

	MEETING_PAYMENT_DONE("정산 완료"),
	MENTIONED("멘션 알림");

	private final String label;

	NotificationType(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return label;
	}
}
