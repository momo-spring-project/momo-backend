package com.example.momo.domain.messagehub.application.dto;

import java.time.LocalDateTime;

import com.example.momo.domain.messagehub.enums.AlarmType;
import com.example.momo.global.rabbitMQ.dto.messagehub.MessageHubNotificationEvent;

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
	private LocalDateTime meetingStartTime;
	@Nullable
	private AlarmType alarmType;

	public MessageHubNotificationEvent toEvent(String content, String type) {
		return MessageHubNotificationEvent.builder()
			.userId(this.userId)
			.targetId(this.meetingId)
			.content(content)
			.type(type)
			.build();
	}

	public void updateAlarmType(AlarmType type) {
		this.alarmType = type;
	}
}
