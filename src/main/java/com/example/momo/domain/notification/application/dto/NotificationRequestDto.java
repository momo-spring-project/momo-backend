package com.example.momo.domain.notification.application.dto;

import com.example.momo.domain.notification.domain.Notification;
import com.example.momo.domain.notification.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NotificationRequestDto {
	private Long userId;
	private Long targetId;
	private NotificationType type;
	private String content;

	public Notification toEntity() {
		return Notification.builder()
			.userId(this.userId)
			.targetId(this.targetId)
			.type(this.type)
			.content(this.content)
			.build();
	}
}