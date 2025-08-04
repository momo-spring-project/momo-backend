package com.example.momo.domain.notification.application.sse.dto;

import com.example.momo.domain.notification.application.dto.NotificationResponseDto;
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

	public static SseMessageDto from(NotificationResponseDto dto) {
		return SseMessageDto.builder()
			.id(dto.getId())
			.userId(dto.getUserId())
			.targetId(dto.getTargetId())
			.type(dto.getType())
			.content(dto.getContent())
			.build();
	}
}