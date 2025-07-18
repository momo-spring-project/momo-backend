package com.example.momo.domain.notification.dto.meeting;

import com.example.momo.domain.notification.entity.Notification;

public record NotificationMeetingCommand(Long userId, Long meetingId, String content) {

	public Notification toEntity() {
		return new Notification(this.userId, this.meetingId, this.content);
	}
}
