package com.example.momo.domain.meetings.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.meetings.domain.Meeting;
import com.example.momo.domain.meetings.domain.MeetingRepository;
import com.example.momo.domain.meetings.enums.MeetingStatus;
import com.example.momo.domain.meetings.presentation.dto.request.MeetingCreateRequest;
import com.example.momo.domain.meetings.presentation.dto.request.MeetingUpdateRequest;
import com.example.momo.domain.meetings.presentation.dto.response.MeetingResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

	private final MeetingRepository meetingRepository;

	@Override
	@Transactional
	public MeetingResponse createMeeting(MeetingCreateRequest request, Long userId) {

		Meeting meeting = request.toMeeting(userId);
		Meeting savedMeeting = meetingRepository.save(meeting);
		return new MeetingResponse(savedMeeting);
	}

	@Override
	@Transactional
	public MeetingResponse updateMeeting(MeetingUpdateRequest request, Long meetingId, Long userId) {

		// TODO : 예외처리
		Meeting meeting = meetingRepository.findByMeetingId(meetingId).orElseThrow();
		meeting.updateMeeting(request, userId);
		return new MeetingResponse(meeting);
	}

	@Override
	public MeetingResponse searchMeeting(Long meetingId) {

		// TODO : 예외처리
		Meeting meeting = meetingRepository.findByMeetingId(meetingId).orElseThrow();
		return new MeetingResponse(meeting);
	}

	@Override
	@Transactional
	public MeetingResponse updateMeetingStatus(Long meetingId, MeetingStatus status) {

		// TODO : 예외처리
		Meeting meeting = meetingRepository.findByMeetingId(meetingId).orElseThrow();
		meeting.updateStatus(status);
		return new MeetingResponse(meeting);
	}
}
