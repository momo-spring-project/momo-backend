package com.example.momo.domain.notification.domain.dto;

import com.example.momo.domain.notification.domain.Notification;

public record NotificationDto(Long userId, Long meetingId, String content) {

	public Notification toEntity() {
		return new Notification(this.userId, this.meetingId, this.content);
	}

}
