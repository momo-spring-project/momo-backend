package com.example.momo.domain.meeting.application;

import com.example.momo.domain.meeting.domain.dto.response.ParticipantResponseDto;

import java.util.List;

public interface MeetingParticipantService {
	ParticipantResponseDto registerParticipant(Long userId, Long meetingId);

	List<Long> getParticipants(Long meetingId);

	ParticipantResponseDto cancelParticipant(Long userId, Long meetingId);

	ParticipantResponseDto updateParticipantStatus(Long id, Long meetingId, double lat, double lng);
}
