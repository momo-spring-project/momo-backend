package com.example.momo.global.socket.application.dto;

import java.time.LocalDateTime;

import com.example.momo.domain.notification.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

//WebSocket 으로 보내는 DTO
@Getter
@Builder
@AllArgsConstructor
public class WebSocketNotificationDto {
	private Long userId;
	private Long targetId;
	private NotificationType type;
	private String content;
	private LocalDateTime createdAt;
}
