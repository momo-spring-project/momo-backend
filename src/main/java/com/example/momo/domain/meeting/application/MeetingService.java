package com.example.momo.domain.meeting.application;

import java.time.LocalDateTime;
import java.util.List;

import com.example.momo.domain.auth.domain.dto.AuthUser;
import com.example.momo.domain.meeting.domain.dto.response.*;
import com.example.momo.domain.meeting.enums.MeetingStatus;
import com.example.momo.domain.meeting.domain.dto.request.MeetingCreateRequest;
import com.example.momo.domain.meeting.domain.dto.request.MeetingUpdateRequest;

public interface MeetingService {

	/** Meeting Service */

	MeetingResponse createMeeting(MeetingCreateRequest request, Long userId);

	MeetingResponse updateMeeting(MeetingUpdateRequest request, Long meetingId, Long userId);

	MeetingResponse searchMeeting(Long meetingId);

	MeetingResponse updateMeetingStatus(Long meetingId, MeetingStatus status, Long userId);

	MeetingPagingResponse<MeetingResponse> getMeetings(String title, MeetingStatus status, LocalDateTime meetingDate,
		int page, int size);

	void deleteMeeting(Long meetingId, Long userId);

	/** Meeting Participant Service */

	ParticipantResponseDto createParticipant(Long userId, Long meetingId);

	ParticipantResponseDto getParticipant(Long participantId);

	List<ParticipantResponseDto> getParticipants(Long meetingId);

	ParticipantResponseDto deleteParticipant(Long userId, Long meetingId);

	ParticipantResponseDto updateParticipantStatus(Long id, Long meetingId, double lat, double lng);

	ParticipantCountResponseDto getParticipantCount(Long userId, Long meetingId, Boolean attendance, LocalDateTime createdAt);
}
