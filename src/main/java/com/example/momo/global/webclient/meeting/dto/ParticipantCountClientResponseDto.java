package com.example.momo.global.webclient.meeting.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantCountClientResponseDto {
	private Long meetingId;
	private Long counts;
	private Boolean attendanceStatus;
	private LocalDateTime createdAt;
}
