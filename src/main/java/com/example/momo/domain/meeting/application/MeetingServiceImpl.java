package com.example.momo.domain.meeting.application;

import java.time.LocalDateTime;
import java.util.List;

import com.example.momo.domain.meeting.domain.MeetingParticipant;
import com.example.momo.domain.meeting.domain.dto.response.*;
import com.example.momo.domain.payment.application.PaymentService;
import com.example.momo.domain.payment.domain.dto.CardPaymentTestRequest;
import com.example.momo.domain.payment.domain.dto.RefundRequest;
import com.example.momo.global.infrastructure.client.user.UserClient;
import com.example.momo.global.infrastructure.client.user.dto.UserClientResponseDto;
import com.example.momo.global.infrastructure.springEvent.MeetingEvents;
import com.example.momo.global.utils.HaversineUtils;
import com.example.momo.global.utils.RetryUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.category.application.CategoryService;
import com.example.momo.domain.category.domain.dto.CategoryResponseDto;
import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.domain.MeetingRepository;
import com.example.momo.domain.meeting.domain.dto.request.MeetingCreateRequestDto;
import com.example.momo.domain.meeting.domain.dto.request.MeetingUpdateRequestDto;
import com.example.momo.domain.meeting.enums.MeetingStatus;
import com.example.momo.domain.meeting.exception.MeetingException;
import com.example.momo.domain.meeting.exception.MeetingExceptionCode;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

	private final ApplicationEventPublisher eventPublisher;
	private final MeetingRepository meetingRepository;
	private final MeetingReader meetingReader;
	private final EntityManager em;
	private final UserClient userClient;
	private final PaymentService paymentService;
	private final CategoryService categoryService;

	@Override
	@Transactional
	public MeetingResponseDto createMeeting(MeetingCreateRequestDto request, Long userId) {

		Meeting meeting = request.toMeeting(userId);
		Meeting savedMeeting = meetingRepository.save(meeting);

		// TODO CategoryId를 통한 category name 조회 (http client 요청)로 변경
		CategoryResponseDto category = categoryService.getCategory(request.getCategoryId());

		eventPublisher.publishEvent(new MeetingEvents.Create(
			meeting.getId(),
			request.getCategoryId(),
			category.getCategoryName(),
			meeting.getLatitude(),
			meeting.getLongitude()
		));

		return new MeetingResponseDto(savedMeeting);
	}

	@Override
	@Transactional
	public MeetingResponseDto updateMeeting(MeetingUpdateRequestDto request, Long meetingId, Long userId) {

		Meeting meeting = meetingRepository.findById(meetingId).orElseThrow(() ->
			new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND));

		if (!meeting.getHostUserId().equals(userId)) {
			throw new MeetingException(MeetingExceptionCode.MEETING_FORBIDDEN);
		}

		meeting.updateMeeting(request);

		eventPublisher.publishEvent(new MeetingEvents.Update(
			meeting.getId(),
			meeting.getTitle(),
			meeting.getParticipants().stream().map(MeetingParticipant::getId).toList()
		));

		return new MeetingResponseDto(meeting);
	}

	@Override
	@Transactional(readOnly = true)
	public MeetingResponseDto getMeeting(Long meetingId) {

		Meeting meeting = meetingRepository.findById(meetingId).orElseThrow(() ->
			new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND));
		return new MeetingResponseDto(meeting);
	}

	@Override
	@Transactional
	public MeetingResponseDto updateMeetingStatus(Long meetingId, MeetingStatus status, Long userId) {

		Meeting meeting = meetingRepository.findById(meetingId).orElseThrow(() ->
			new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND));

		if (!meeting.getHostUserId().equals(userId)) {
			throw new MeetingException(MeetingExceptionCode.MEETING_FORBIDDEN);
		}

		meeting.updateStatus(status);
		return new MeetingResponseDto(meeting);
	}

	@Override
	@Transactional(readOnly = true)
	public MeetingPagingResponseDto<MeetingResponseDto> getMeetings(String title, MeetingStatus status,
		LocalDateTime meetingDate, int page, int size) {

		Pageable pageable = PageRequest
			.of(page - 1, size, Sort.Direction.DESC, "createdAt");

		Page<Meeting> meetingPage = meetingRepository.getMeetings(title, meetingDate, status, pageable);
		List<MeetingResponseDto> meetingResponses = meetingPage.stream()
			.map(MeetingResponseDto::new)
			.toList();

		return new MeetingPagingResponseDto<>(
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

		eventPublisher.publishEvent(new MeetingEvents.Delete(
			meeting.getId(),
			meeting.getTitle(),
			meeting.getParticipants().stream().map(MeetingParticipant::getId).toList()
		));
	}

	/**
	 * Meeting Participant Service
	 */

	@Override
	@Transactional
	public ParticipantCreateResponseDto createParticipant(Long userId, Long meetingId) {

		// 이미 참가했으면 예외처리
		if (meetingRepository.existsByMeetingIdAndUserId(meetingId, userId)) {
			throw new MeetingException(MeetingExceptionCode.ALREADY_PARTICIPATED);
		}

		// 모임 활성화 확인
		Meeting meeting = meetingReader.getMeetingById(meetingId);
		isMeetingOpen(meeting);

		// 유저 자격 확인
		UserClientResponseDto user = userClient.getUser(userId);
		if (user.getScore() < meeting.getMinEnterScore()) {
			throw new MeetingException(MeetingExceptionCode.INSUFFICIENT_SCORE);
		}
		System.out.println("UserId = " + userId);

		eventPublisher.publishEvent(new MeetingEvents.Register(meetingId, userId));

		// createParticipant 에서는 결제 요청 까지만 진행
		return new ParticipantCreateResponseDto("PENDING", "결제 진행 중...");
	}

	@Override
	public ParticipantResponseDto getParticipant(Long participantId) {

		MeetingParticipant participant = meetingReader.getParticipantById(participantId);

		return new ParticipantResponseDto(participant);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ParticipantResponseDto> getParticipants(Long meetingId) {
		if(!meetingRepository.existsById(meetingId)) {
			throw new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND);
		}

		List<MeetingParticipant> participants = meetingRepository.findAllParticipantsByMeetingId(meetingId);

		return participants.stream()
			.map(ParticipantResponseDto::new)
			.toList();
	}

	@Override
	@Transactional
	public ParticipantResponseDto deleteParticipant(Long userId, Long meetingId) {

		Meeting meeting = meetingReader.getMeetingById(meetingId);
		UserClientResponseDto user = userClient.getUser(userId);

		MeetingParticipant participant = meetingReader.getParticipantByMeetingIdAndUserId(meetingId, userId);

		ParticipantResponseDto responseDto = RetryUtil.retry(() -> removeParticipant(meetingId, participant), 5);

		eventPublisher.publishEvent(new MeetingEvents.Cancel(meetingId, meeting.getHostUserId(), user.getNickname()));

		return responseDto;
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
		if (!HaversineUtils.isInDistance(destLat, destLng, lat, lng, 10000000)) {
			throw new MeetingException(MeetingExceptionCode.FAR_FROM_MEETING);
		}

		MeetingParticipant participant = meetingReader.getParticipantByMeetingIdAndUserId(meetingId, userId);

		participant.updateAttendanceStatus();

		return new ParticipantResponseDto(participant);
	}

	@Override
	@Transactional(readOnly = true)
	public ParticipantCountResponseDto getParticipantCount(Long userId, Long meetingId, Boolean attendance, LocalDateTime createdAt) {

		if(createdAt == null) {
			createdAt = LocalDateTime.now().minusYears(1);
		}

		Long counts = meetingRepository.countParticipants(userId, meetingId, attendance, createdAt);

		return new ParticipantCountResponseDto(userId, meetingId, counts, attendance, createdAt);
	}

	// 참가자 추가
	@Override
	@Transactional
	public ParticipantResponseDto addParticipant(Long meetingId, Long userId) {

		Meeting meeting = meetingReader.getMeetingById(meetingId);

		if (meeting.getCurrentParticipantsCount() >= meeting.getMaxParticipantsCount()) {
			throw new MeetingException(MeetingExceptionCode.MEETING_IS_FULL);
		}

		// 참가자 추가
		MeetingParticipant participant = new MeetingParticipant(meeting.getId(), userId);

		meeting.addMeetingParticipant();
		MeetingParticipant savedParticipant = meetingRepository.saveParticipant(participant);

		return new ParticipantResponseDto(savedParticipant);
	}

	// 참가자 감소
	@Transactional
	public ParticipantResponseDto removeParticipant(Long meetingId, MeetingParticipant participant) {

		Meeting meeting = meetingReader.getMeetingById(meetingId);

		if (meeting.getCurrentParticipantsCount() <= 0) {
			throw new MeetingException(MeetingExceptionCode.INVALID_PARTICIPANT_COUNT);
		}

		// 인원 계산, 참가자 삭제
		meeting.getParticipants().remove(participant);
		meeting.removeMeetingParticipant();

		return new ParticipantResponseDto(participant);
	}

	// 모임이 활성화 되어있는 상태인지 확인
	private void isMeetingOpen(Meeting meeting) {

		// 시작 시간 넘김
		if (LocalDateTime.now().isAfter(meeting.getMeetingDate())) {
			throw new MeetingException(MeetingExceptionCode.ALREADY_STARTED_MEETING);
		}

		// 모임 상태 FINISHED
		if (meeting.getStatus() == MeetingStatus.FINISHED) {
			throw new MeetingException(MeetingExceptionCode.ALREADY_FINISHED_MEETING);
		}

		// 삭제된 모임
		if (meeting.getIsDeleted()) {
			throw new MeetingException(MeetingExceptionCode.DELETED_MEETING);
		}
	}
}
