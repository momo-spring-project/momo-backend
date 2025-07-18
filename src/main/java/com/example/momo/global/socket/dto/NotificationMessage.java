package com.example.momo.global.socket.dto;

//WebSocket 으로 보내는 DTO
public record NotificationMessage(Long userId, String content,
                                  String timestamp) {
}
