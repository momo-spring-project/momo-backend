package com.example.momo.domain.notification.dto.meeting;

//WebSocket 으로 보내는 DTO
public record NotificationMessage(Long userId, String content,
								  String timestamp) {
}
