package com.example.momo.domain.notification.dto;

import com.example.momo.domain.notification.entity.Notification;

public record NotificationEvent(Long userId, Long meetingId, String comment) {

	public Notification toEntity() {
		return new Notification(this.userId, this.meetingId, this.comment);
	}
}