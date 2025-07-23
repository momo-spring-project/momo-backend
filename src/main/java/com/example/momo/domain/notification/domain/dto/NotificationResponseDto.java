package com.example.momo.domain.notification.domain.dto;

import java.time.LocalDateTime;

import com.example.momo.domain.notification.domain.Notification;

public record NotificationResponseDto(Long id, Long meetingId, String content, LocalDateTime createdAt) {
	public static NotificationResponseDto from(Notification notification) {
		return new NotificationResponseDto(
			notification.getId(),
			notification.getMeetingId(),
			notification.getContent(),
			notification.getCreatedAt()
		);
	}
}
