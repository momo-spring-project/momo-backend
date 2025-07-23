package com.example.momo.domain.meeting.application;

import java.time.LocalDateTime;
import java.util.List;

import com.example.momo.domain.meeting.domain.MeetingParticipant;
import com.example.momo.domain.meeting.domain.dto.response.ParticipantCreateResponseDto;
import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.domain.UserRepository;
import com.example.momo.global.utils.HaversineUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.example.momo.domain.meeting.domain.dto.response.ParticipantResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.domain.MeetingRepository;
import com.example.momo.domain.meeting.enums.MeetingStatus;
import com.example.momo.domain.meeting.exception.MeetingException;
import com.example.momo.domain.meeting.exception.MeetingExceptionCode;
import com.example.momo.domain.meeting.domain.dto.request.MeetingCreateRequest;
import com.example.momo.domain.meeting.domain.dto.request.MeetingUpdateRequest;
import com.example.momo.domain.meeting.domain.dto.response.MeetingPagingResponse;
import com.example.momo.domain.meeting.domain.dto.response.MeetingResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

	private final MeetingRepository meetingRepository;
	private final MeetingReader meetingReader;

	private final UserRepository userRepository;

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
	@Transactional
	public ParticipantCreateResponseDto createParticipant(Long userId, Long meetingId) {

		Meeting meeting = meetingReader.getMeetingById(meetingId);

		// Todo 구조 전환시 유저 조회 예외처리 수정 또는 제거 필요함
		User user = userRepository.findByIdAndIsDeletedFalse(userId)
			.orElseThrow(() -> new RuntimeException("user not found"));

		// 이미 참가했으면 예외처리
		if(meetingRepository.existsByMeetingIdAndUserId(meetingId, userId)) {
			throw new MeetingException(MeetingExceptionCode.ALREADY_PARTICIPATED);
		}

		// 모임 활성화 확인
		isMeetingOpen(meeting);

		// 유저 자격 확인
		if(user.getScore() < meeting.getMinEnterScore()) {
			throw new MeetingException(MeetingExceptionCode.INSUFFICIENT_SCORE);
		}

		// 이벤트발행(테스트)
		// 결제완료 이벤트 있으면 넣어서 테스트

		//결제 알고리즘

		return new ParticipantCreateResponseDto("PENDING", "결제 진행 중...");
	}

	@Override
	public ParticipantResponseDto getParticipant(Long participantId) {

		MeetingParticipant participant = meetingReader.getParticipantById(participantId);

		return new ParticipantResponseDto(participant);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Long> getParticipants(Long meetingId) {
		if(!meetingRepository.existsById(meetingId)) {
			throw new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND);
		}

		return meetingRepository.findParticipantsIdsByMeetingId(meetingId);
	}

	@Override
	@Transactional
	public ParticipantResponseDto deleteParticipant(Long userId, Long meetingId) {

		Meeting meeting = meetingReader.getMeetingById(meetingId);
		User user = userRepository.findByIdAndIsDeletedFalse(userId)
			.orElseThrow(() -> new RuntimeException("user not found"));

		MeetingParticipant participant = meetingReader.getParticipantByMeetingIdAndUserId(meetingId, userId);

		// 이벤트발행(테스트)

		// 환불 알고리즘

		return new ParticipantResponseDto(participant);
	}

	@Override
	@Transactional
	public ParticipantResponseDto updateParticipantStatus(Long userId, Long meetingId, double lat, double lng) {

		Meeting meeting = meetingReader.getMeetingById(meetingId);

		// 모임 활성화 확인
		isMeetingOpen(meeting);

		Double destLat = meeting.getLatitude();
		Double destLng = meeting.getLongitude();

		// 위치가 목적지 근처인지 확인
		if(!HaversineUtils.isInDistance(destLat, destLng, lat, lng)) {
			throw new MeetingException(MeetingExceptionCode.FAR_FROM_MEETING);
		}

		MeetingParticipant participant = meetingReader.getParticipantByMeetingIdAndUserId(meetingId, userId);

		participant.updateAttendanceStatus();

		return new ParticipantResponseDto(participant);
	}

	// 모임이 활성화 되어있는 상태인지 확인
	private void isMeetingOpen(Meeting meeting) {

		// 시작 시간 넘김
		if(LocalDateTime.now().isAfter(meeting.getMeetingDate())) {
			throw new MeetingException(MeetingExceptionCode.ALREADY_STARTED_MEETING);
		}

		// 모임 상태 FINISHED
		if(meeting.getStatus() == MeetingStatus.FINISHED) {
			throw new MeetingException(MeetingExceptionCode.ALREADY_FINISHED_MEETING);
		}

		// 삭제된 모임
		if(meeting.getIsDeleted()) {
			throw new MeetingException(MeetingExceptionCode.DELETED_MEETING);
		}
	}

}
