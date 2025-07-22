package com.example.momo.domain.meetings.application;

import java.time.LocalDateTime;

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

}
