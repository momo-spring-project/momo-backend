package com.example.momo.domain.messagehub.application.dto;

import java.util.List;

import com.example.momo.domain.messagehub.enums.MessageType;
import com.example.momo.global.rabbitmq.dto.messagehub.MessageHubNotificationMessage;

/**
 * 메세지 허브에서 발생하는 이벤트를 정의합니다.
 */
public record MessageDto(List<Long> userIdList, Long targetId, MessageType type, String content) {
	public MessageHubNotificationMessage toMessage(Long userId) {
		return MessageHubNotificationMessage.builder()
			.userId(userId)
			.targetId(this.targetId)
			.type(this.type.name())
			.content(this.content)
			.build();
	}
}