package com.example.momo.domain.notification.domain.dto;

import com.example.momo.domain.notification.domain.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(Long id, Long meetingId, String content, LocalDateTime createdAt) {
	public static NotificationResponse from(Notification notification) {
		return new NotificationResponse(
			notification.getId(),
			notification.getMeetingId(),
			notification.getContent(),
			notification.getCreatedAt()
		);
	}
}
