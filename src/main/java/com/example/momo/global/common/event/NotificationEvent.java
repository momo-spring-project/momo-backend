package com.example.momo.global.common.event;

import com.example.momo.domain.notification.domain.Notification;

//Event 생성 시 전달받는 DTO
public record NotificationEvent(Long userId, Long meetingId, String content) {

	public Notification toEntity() {
		return new Notification(this.userId, this.meetingId, this.content);
	}
}
