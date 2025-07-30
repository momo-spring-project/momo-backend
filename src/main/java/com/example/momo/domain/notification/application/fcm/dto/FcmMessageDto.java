package com.example.momo.domain.notification.application.fcm.dto;

import com.example.momo.domain.notification.domain.Notification;
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

	public static FcmMessageDto from(Notification notification) {
		return FcmMessageDto.builder()
			.userId(notification.getUserId())
			.type(notification.getType())
			.content(notification.getContent())
			.build();
	}

	public void updateToken(String token) {
		this.token = token;
	}

}