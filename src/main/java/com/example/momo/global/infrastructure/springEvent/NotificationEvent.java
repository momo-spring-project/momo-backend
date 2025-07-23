package com.example.momo.global.infrastructure.springEvent;

//Event 생성 시 전달받는 DTO
public record NotificationEvent(Long userId, Long meetingId, String content) {
}
