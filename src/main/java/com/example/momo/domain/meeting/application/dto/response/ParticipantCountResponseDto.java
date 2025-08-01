package com.example.momo.domain.meeting.application.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantCountResponseDto {
	private Long userId;
	private Long meetingId;
	private Long counts;
	private Boolean attendanceStatus;
	private LocalDateTime createdAt;
}