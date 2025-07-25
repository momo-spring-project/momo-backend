package com.example.momo.domain.notification.domain.dto;

import java.time.LocalDateTime;

import com.example.momo.domain.notification.domain.Notification;
import com.example.momo.domain.notification.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NotificationResponseDto {
	private Long id;
	private Long meetingId;
	private NotificationType type;
	private String content;
	private LocalDateTime createdAt;

	public static NotificationResponseDto from(Notification notification) {
		return NotificationResponseDto.builder()
			.id(notification.getId())
			.meetingId(notification.getMeetingId())
			.type(notification.getType())
			.content(notification.getContent())
			.createdAt(notification.getCreatedAt())
			.build();
	}
}
