package com.example.momo.global.webclient.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantClientResponseDto {
	private Long id;
	private Long meetingId;
	private Long participantId;
	private boolean attendanceStatus;
}
