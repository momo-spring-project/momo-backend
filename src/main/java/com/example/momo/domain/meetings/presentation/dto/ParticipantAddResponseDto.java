package com.example.momo.domain.meetings.presentation.dto;

import com.example.momo.domain.meetings.domain.MeetingParticipant;
import lombok.Getter;

@Getter
public class ParticipantAddResponseDto {
	private final Long id;
	private final Long meetingId;
	private final Long participantId;
	private final boolean attendanceStatus;

	public ParticipantAddResponseDto(MeetingParticipant participant) {
		this.id = participant.getId();
		this.meetingId = participant.getMeetingId();
		this.participantId = participant.getUserId();
		this.attendanceStatus = participant.getAttendanceStatus();
	}
}
