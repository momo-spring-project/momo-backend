package com.example.momo.domain.notification.application.fcm.dto;

import com.example.momo.domain.notification.application.dto.NotificationResponseDto;
import com.example.momo.domain.notification.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FcmMessageDto {
	private Long userId;
	private String token;
	private NotificationType type;
	private String content;

	public static FcmMessageDto from(NotificationResponseDto dto) {
		return FcmMessageDto.builder()
			.userId(dto.getUserId())
			.type(dto.getType())
			.content(dto.getContent())
			.build();
	}

	public void updateToken(String token) {
		this.token = token;
	}

}