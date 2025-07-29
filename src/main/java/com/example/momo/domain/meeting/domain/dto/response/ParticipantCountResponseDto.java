package com.example.momo.domain.meeting.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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