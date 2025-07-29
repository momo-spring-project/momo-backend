package com.example.momo.global.infrastructure.client.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantCountClientResponseDto {
	private Long meetingId;
	private Long counts;
	private Boolean attendanceStatus;
	private LocalDateTime createdAt;
}
