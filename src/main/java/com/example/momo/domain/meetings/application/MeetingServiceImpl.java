package com.example.momo.domain.meetings.application;

import java.time.LocalDateTime;
import java.util.List;

import com.example.momo.domain.meetings.presentation.dto.ParticipantResponseDto;
import org.springframework.stereotype.Service;

import com.example.momo.domain.meetings.enums.MeetingStatus;
import com.example.momo.domain.meetings.presentation.dto.request.MeetingCreateRequest;
import com.example.momo.domain.meetings.presentation.dto.request.MeetingUpdateRequest;
import com.example.momo.domain.meetings.presentation.dto.response.MeetingPagingResponse;
import com.example.momo.domain.meetings.presentation.dto.response.MeetingResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

	private final MeetingCoreService meetingCoreService;
	private final MeetingParticipantService meetingParticipantService;

	/**
	 * Meeting Core Service
	 */

	@Override
	public MeetingResponse createMeeting(MeetingCreateRequest request, Long userId) {

		return meetingCoreService.createMeeting(request, userId);
	}

	@Override
	public MeetingResponse updateMeeting(MeetingUpdateRequest request, Long meetingId, Long userId) {

		return meetingCoreService.updateMeeting(request, meetingId, userId);
	}

	@Override
	public MeetingResponse searchMeeting(Long meetingId) {

		return meetingCoreService.searchMeeting(meetingId);
	}

	@Override
	public MeetingResponse updateMeetingStatus(Long meetingId, MeetingStatus status, Long userId) {

		return meetingCoreService.updateMeetingStatus(meetingId, status, userId);
	}

	@Override
	public MeetingPagingResponse<MeetingResponse> getMeetings(String title, MeetingStatus status,
		LocalDateTime meetingDate, int page, int size) {

		return meetingCoreService.getMeetings(title, status, meetingDate, page, size);
	}

	@Override
	public void deleteMeeting(Long meetingId, Long userId) {

		meetingCoreService.deleteMeeting(meetingId, userId);
	}

	/**
	 * Meeting Participant Service
	 */

	@Override
	public ParticipantResponseDto registerParticipant(Long userId, Long meetingId) {
		return meetingParticipantService.registerParticipant(userId, meetingId);
	}

	@Override
	public List<Long> getParticipants(Long meetingId) {
		return meetingParticipantService.getParticipants(meetingId);
	}

	@Override
	public ParticipantResponseDto cancelParticipant(Long userId, Long meetingId) {
		return meetingParticipantService.cancelParticipant(userId, meetingId);
	}

	@Override
	public ParticipantResponseDto updateParticipantStatus(Long id, Long meetingId, double lat, double lng) {
		return meetingParticipantService.updateParticipantStatus(id, meetingId, lat, lng);
	}

}
