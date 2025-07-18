package com.example.momo.domain.meetings.application;

import com.example.momo.domain.meetings.presentation.dto.ParticipantAddResponseDto;

public interface MeetingParticipantService {
	ParticipantAddResponseDto addParticipant(Long userId, Long meetingId);
}
