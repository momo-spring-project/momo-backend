package com.example.momo.domain.notification.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
	// 미팅 관련
	MEETING_JOINED("모임 참가"),
	MEETING_CANCELLED("모임 취소"),
	MEETING_UPDATED("모임 정보 변경"),
	MEETING_DELETED("모임 삭제"),
	MEETING_RECOMMENDED("추천 모임"),

	// 팔로우
	FOLLOWED("팔로우"),

	// 결제 관련
	PAID("결제 완료"),
	REFUNDED("환불 완료");

	private final String label;

	NotificationType(String label) {
		this.label = label;
	}

}
