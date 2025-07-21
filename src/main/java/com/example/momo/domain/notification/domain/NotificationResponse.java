package com.example.momo.domain.notification.domain;

import java.time.LocalDateTime;

public record NotificationResponse(Long id, Long meetingId, String content, LocalDateTime createdAt) {

}
