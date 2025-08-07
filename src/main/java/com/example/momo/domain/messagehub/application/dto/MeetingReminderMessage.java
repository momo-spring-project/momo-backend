package com.example.momo.domain.messagehub.application.dto;

import java.time.LocalDateTime;

import com.example.momo.domain.messagehub.enums.AlarmType;
import com.example.momo.global.rabbitmq.dto.messagehub.MessageHubNotificationMessage;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingReminderMessage {

	private Long userId;
	private Long meetingId;
	private String meetingName;
	@Nullable
	private LocalDateTime meetingDate;
	@Nullable
	private AlarmType alarmType;

	public MessageHubNotificationMessage toEvent(String content, String type) {
		return MessageHubNotificationMessage.builder()
			.userId(this.userId)
			.targetId(this.meetingId)
			.content(content)
			.type(type)
			.build();
	}
}
