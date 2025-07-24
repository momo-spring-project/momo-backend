package com.example.momo.domain.meeting.application;

import java.time.LocalDateTime;
import java.util.List;

import com.example.momo.domain.meeting.domain.dto.response.ParticipantCreateResponseDto;
import com.example.momo.domain.meeting.enums.MeetingStatus;
import com.example.momo.domain.meeting.domain.dto.response.ParticipantResponseDto;
import com.example.momo.domain.meeting.domain.dto.request.MeetingCreateRequest;
import com.example.momo.domain.meeting.domain.dto.request.MeetingUpdateRequest;
import com.example.momo.domain.meeting.domain.dto.response.MeetingPagingResponse;
import com.example.momo.domain.meeting.domain.dto.response.MeetingResponse;

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

	List<Long> getParticipants(Long meetingId);

	ParticipantResponseDto deleteParticipant(Long userId, Long meetingId);

	ParticipantResponseDto updateParticipantStatus(Long id, Long meetingId, double lat, double lng);
}
