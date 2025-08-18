package com.example.momo.domain.notification.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum NotificationType {
	// 미팅 관련
	MEETING_JOINED("모임 참가"),
	MEETING_CANCELLED("모임 취소"),
	MEETING_UPDATED("모임 정보 변경"),
	MEETING_DELETED("모임 삭제"),
	MEETING_RECOMMENDED("추천 모임"),
	MEETING_UPCOMING("모임 예정"),
	MEETING_TOMORROW("내일 모임 예정"),

	// 팔로우
	FOLLOWED("팔로우"),

	// 결제 관련
	PAID("결제 완료"),
	REFUNDED("환불 완료");

	private final String label;

	NotificationType(String label) {
		this.label = label;
	}

	@JsonCreator
	public static NotificationType from(String name) {
		if (name == null) {
			return null;
		}
		return NotificationType.valueOf(name.trim().toUpperCase());

	}

	@JsonValue
	public String toJson() {
		return name();
	}
}
