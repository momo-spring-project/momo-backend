package com.example.momo.domain.notification.domain;

import java.time.LocalDateTime;

public record NotificationResponse(Long id, Long meetingId, String content, LocalDateTime createdAt) {
	public NotificationResponse(Notification notification) {
		this(
			notification.getId(),
			notification.getMeetingId(),
			notification.getContent(),
			notification.getCreatedAt()
		);
	}
}
