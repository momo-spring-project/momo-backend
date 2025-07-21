package com.example.momo.global.event;

import java.time.LocalDateTime;

import com.example.momo.domain.notification.domain.Notification;
import com.example.momo.global.socket.dto.NotificationMessage;

//Event 생성 시 전달받는 DTO
public record NotificationMeetingEvent(Long userId, Long meetingId, String content) {

	public NotificationMessage toMessage() {
		return new NotificationMessage(this.userId, this.content, LocalDateTime.now());
	}

	public Notification toEntity() {
		return new Notification(this.userId, this.meetingId, this.content);
	}
}
