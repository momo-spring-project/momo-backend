package com.example.momo.global.firebase.application.dto;

import com.example.momo.domain.notification.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FirebaseResponseDto {
	private String token;
	private Long targetId;
	private NotificationType type;
	private String content;
}