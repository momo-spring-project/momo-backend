package com.example.momo.global.socket.dto;

import java.time.LocalDateTime;

//WebSocket 으로 보내는 DTO
public record WebSocketNotificationDto(Long userId, String content,
                                       LocalDateTime createdAt) {
}
