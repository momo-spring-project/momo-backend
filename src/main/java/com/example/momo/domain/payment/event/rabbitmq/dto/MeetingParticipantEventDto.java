package com.example.momo.domain.payment.event.rabbitmq.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingParticipantEventDto {
	private Long participantId;
	private Long userId;
	private Long meetingId;
	private Integer amount;  // 참가비
	private String eventType;  // PARTICIPANT_CREATED, PARTICIPANT_REMOVED 등
	private LocalDateTime occurredAt;
}