package com.example.momo.global.rabbitMQ.dto.notification;

public record NotificationQueueEvent(Long userId, Long targetId, String typeName, String content) {
}
