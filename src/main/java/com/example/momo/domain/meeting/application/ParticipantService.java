package com.example.momo.domain.meeting.application;

import com.example.momo.domain.meeting.domain.MeetingParticipant;
import com.example.momo.domain.meeting.domain.dto.response.ParticipantResponseDto;

public interface ParticipantService {

	ParticipantResponseDto addParticipant(Long meetingId, Long userId);

	ParticipantResponseDto removeParticipant(Long meetingId, MeetingParticipant participant);
}
