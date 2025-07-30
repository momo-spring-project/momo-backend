package com.example.momo.domain.notification.application.fcm.dto;

import com.example.momo.domain.notification.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FcmMessageDto {
	private String token;
	private NotificationType type;
	private String content;
}