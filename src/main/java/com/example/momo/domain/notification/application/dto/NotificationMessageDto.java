package com.example.momo.domain.notification.application.dto;

import com.example.momo.domain.notification.enums.NotificationType;
import com.example.momo.global.rabbitMQ.dto.notification.NotificationQueueEvent;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessageDto {

	private Long userId;
	private Long targetId;
	private String content;
	private NotificationType type;
	@Nullable
	private Long notificationId;

	public void updateNotificationId(Long notificationId) {
		this.notificationId = notificationId;
	}

	public static NotificationMessageDto of(NotificationQueueEvent event) {
		return NotificationMessageDto.builder()
			.userId(event.getUserId())
			.targetId(event.getTargetId())
			.type(event.getType())
			.content(event.getContent())
			.notificationId(event.getNotificationId())
			.build();
	}
}
