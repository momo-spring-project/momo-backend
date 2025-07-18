package com.example.momo.domain.meetings.application;

import com.example.momo.domain.meetings.presentation.dto.ParticipantResponseDto;

import java.util.List;

public interface MeetingParticipantService {
	ParticipantResponseDto addParticipant(Long userId, Long meetingId);

	List<Long> getParticipants(Long meetingId);

	ParticipantResponseDto cancelParticipant(Long userId, Long meetingId);
}
