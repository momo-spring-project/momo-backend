package com.example.momo.domain.meetings.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.example.momo.domain.meetings.presentation.dto.ParticipantResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.meetings.domain.Meeting;
import com.example.momo.domain.meetings.domain.MeetingRepository;
import com.example.momo.domain.meetings.enums.MeetingStatus;
import com.example.momo.domain.meetings.exception.MeetingException;
import com.example.momo.domain.meetings.exception.MeetingExceptionCode;
import com.example.momo.domain.meetings.presentation.dto.request.MeetingCreateRequest;
import com.example.momo.domain.meetings.presentation.dto.request.MeetingUpdateRequest;
import com.example.momo.domain.meetings.presentation.dto.response.MeetingPagingResponse;
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

		Meeting meeting = meetingRepository.findById(meetingId).orElseThrow(() ->
			new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND));

		if (!meeting.getHostUserId().equals(userId)) {
			throw new MeetingException(MeetingExceptionCode.MEETING_FORBIDDEN);
		}

		meeting.updateMeeting(request);

		return new MeetingResponse(meeting);
	}

	@Override
	@Transactional(readOnly = true)
	public MeetingResponse searchMeeting(Long meetingId) {

		Meeting meeting = meetingRepository.findById(meetingId).orElseThrow(() ->
			new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND));
		return new MeetingResponse(meeting);
	}

	@Override
	@Transactional
	public MeetingResponse updateMeetingStatus(Long meetingId, MeetingStatus status, Long userId) {

		Meeting meeting = meetingRepository.findById(meetingId).orElseThrow(() ->
			new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND));

		if (!meeting.getHostUserId().equals(userId)) {
			throw new MeetingException(MeetingExceptionCode.MEETING_FORBIDDEN);
		}

		meeting.updateStatus(status);
		return new MeetingResponse(meeting);
	}

	@Override
	@Transactional(readOnly = true)
	public MeetingPagingResponse<MeetingResponse> getMeetings(String title, MeetingStatus status,
		LocalDateTime meetingDate, int page, int size) {

		Pageable pageable = PageRequest
			.of(page - 1, size, Sort.Direction.DESC, "createdAt");

		Page<Meeting> meetingPage = meetingRepository.findMeetings(title, meetingDate, status, pageable);
		List<MeetingResponse> meetingResponses = meetingPage.stream()
			.map(MeetingResponse::new)
			.toList();

		return new MeetingPagingResponse<>(
			meetingResponses,
			meetingPage.getTotalElements(),
			meetingPage.getTotalPages(),
			meetingPage.getNumber() + 1
		);
	}

	@Override
	@Transactional
	public void deleteMeeting(Long meetingId, Long userId) {

		Meeting meeting = meetingRepository.findById(meetingId).orElseThrow(() ->
			new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND));

		if (!meeting.getHostUserId().equals(userId)) {
			throw new MeetingException(MeetingExceptionCode.MEETING_FORBIDDEN);
		}

		meeting.delete();
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
