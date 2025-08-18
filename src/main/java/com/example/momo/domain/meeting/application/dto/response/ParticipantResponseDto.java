package com.example.momo.domain.meeting.application.dto.response;

import com.example.momo.domain.meeting.domain.MeetingParticipant;

import lombok.Getter;

@Getter
public class ParticipantResponseDto {
	private final Long id;
	private final Long meetingId;
	private final Long participantId;
	private final boolean attendanceStatus;

	public ParticipantResponseDto(MeetingParticipant participant) {
		this.id = participant.getId();
		this.meetingId = participant.getMeeting().getId();
		this.participantId = participant.getUserId();
		this.attendanceStatus = participant.getAttendanceStatus();
	}
}
