package com.example.momo.domain.messagehub.application.dto;

import java.time.LocalDateTime;

import com.example.momo.domain.messagehub.enums.AlarmType;
import com.example.momo.global.rabbitmq.dto.messagehub.MessageHubNotificationMessage;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 모임 알림(30분 전/하루 전) 전송에 사용되는 메시지 DTO.
 * Redis 저장·조회 및 메시지 허브 이벤트 변환에 활용.
 */
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

	public static MeetingReminderMessage of(Object obj) {
		if (obj instanceof MeetingReminderMessage m) {
			return m;
		}
		return null;
	}

}
