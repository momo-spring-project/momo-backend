package com.example.momo.domain.notification.application.sse.dto;

import com.example.momo.domain.notification.domain.Notification;
import com.example.momo.domain.notification.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SseMessageDto {
	private Long id;
	private Long userId;
	private Long targetId;
	private NotificationType type;
	private String content;

	public static SseMessageDto from(Notification notification) {
		return SseMessageDto.builder()
			.id(notification.getId())
			.userId(notification.getUserId())
			.targetId(notification.getTargetId())
			.type(notification.getType())
			.content(notification.getContent())
			.build();
	}
}