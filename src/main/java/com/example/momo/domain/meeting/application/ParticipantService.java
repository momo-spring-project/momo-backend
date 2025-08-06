package com.example.momo.domain.meeting.application;

import com.example.momo.domain.meeting.application.dto.response.ParticipantResponseDto;
import com.example.momo.domain.meeting.domain.MeetingParticipant;

public interface ParticipantService {

	ParticipantResponseDto removeParticipant(Long meetingId, MeetingParticipant participant);
}
