package com.example.momo.domain.messagehub.application.dto;

import java.util.List;

import com.example.momo.domain.messagehub.enums.MessageType;
import com.example.momo.global.rabbitMQ.dto.notification.NotificationQueueEvent;

/**
 * 메세지 허브에서 발생하는 이벤트를 정의합니다.
 */
public record MessageDto(List<Long> userIdList, Long targetId, MessageType type, String content) {
	public NotificationQueueEvent toMessage(Long userId) {
		return new NotificationQueueEvent(userId, this.targetId, this.type.name(), this.content);
	}
}
