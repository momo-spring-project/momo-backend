package com.example.momo.domain.meetings.application;

import java.time.LocalDateTime;
import java.util.List;

import com.example.momo.domain.meetings.enums.MeetingStatus;
import com.example.momo.domain.meetings.presentation.dto.ParticipantResponseDto;
import com.example.momo.domain.meetings.presentation.dto.request.MeetingCreateRequest;
import com.example.momo.domain.meetings.presentation.dto.request.MeetingUpdateRequest;
import com.example.momo.domain.meetings.presentation.dto.response.MeetingPagingResponse;
import com.example.momo.domain.meetings.presentation.dto.response.MeetingResponse;

public interface MeetingService {

	/* Meeting Core Service */

	MeetingResponse createMeeting(MeetingCreateRequest request, Long userId);

	MeetingResponse updateMeeting(MeetingUpdateRequest request, Long meetingId, Long userId);

	MeetingResponse searchMeeting(Long meetingId);

	MeetingResponse updateMeetingStatus(Long meetingId, MeetingStatus status, Long userId);

	MeetingPagingResponse<MeetingResponse> getMeetings(String title, MeetingStatus status, LocalDateTime meetingDate,
		int page, int size);

	void deleteMeeting(Long meetingId, Long userId);

	/* Meeting Participant Service */

	ParticipantResponseDto registerParticipant(Long userId, Long meetingId);

	List<Long> getParticipants(Long meetingId);

	ParticipantResponseDto cancelParticipant(Long userId, Long meetingId);

	ParticipantResponseDto updateParticipantStatus(Long id, Long meetingId, double lat, double lng);
}
