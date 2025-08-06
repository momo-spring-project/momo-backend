package com.example.momo.domain.meeting.application;

import java.time.LocalDateTime;
import java.util.List;

import com.example.momo.domain.meeting.event.rabbitmq.producer.MeetingEventPublisher;
import com.example.momo.global.rabbitmq.dto.ParticipantEvents;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.meeting.application.dto.request.MeetingCreateRequestDto;
import com.example.momo.domain.meeting.application.dto.request.MeetingUpdateRequestDto;
import com.example.momo.domain.meeting.application.dto.response.MeetingPagingResponseDto;
import com.example.momo.domain.meeting.application.dto.response.MeetingResponseDto;
import com.example.momo.domain.meeting.application.dto.response.ParticipantCountResponseDto;
import com.example.momo.domain.meeting.application.dto.response.ParticipantCreateResponseDto;
import com.example.momo.domain.meeting.application.dto.response.ParticipantResponseDto;
import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.domain.MeetingDocument;
import com.example.momo.domain.meeting.domain.MeetingParticipant;
import com.example.momo.domain.meeting.domain.MeetingRepository;
import com.example.momo.domain.meeting.enums.MeetingStatus;
import com.example.momo.domain.meeting.exception.MeetingException;
import com.example.momo.domain.meeting.exception.MeetingExceptionCode;
import com.example.momo.global.springEvent.MeetingEvents;
import com.example.momo.global.springEvent.meeting.RegisterEvents;
import com.example.momo.global.utils.HaversineUtils;
import com.example.momo.global.utils.RetryUtil;
import com.example.momo.global.webclient.category.CategoryClient;
import com.example.momo.global.webclient.category.dto.CategoryClientResponseDto;
import com.example.momo.global.webclient.user.UserClient;
import com.example.momo.global.webclient.user.dto.UserClientResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

	private final ApplicationEventPublisher eventPublisher;
	private final MeetingRepository meetingRepository;
	private final MeetingReader meetingReader;
	private final UserClient userClient;
	private final ParticipantService participantService;
	private final CategoryClient categoryClient;
	private final MeetingEventPublisher meetingEventPublisher;

	@Override
	@Transactional
	public MeetingResponseDto createMeeting(MeetingCreateRequestDto request, Long userId) {

		CategoryClientResponseDto category = categoryClient.getCategory(request.getCategoryId());

		Meeting meeting = request.toMeeting(userId);
		Meeting savedMeeting = meetingRepository.save(meeting);

		meetingRepository.saveMeetingElastic(meeting);

		eventPublisher.publishEvent(new MeetingEvents.Create(
			meeting.getId(),
			request.getCategoryId(),
			category.getName(),
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
		meetingRepository.saveMeetingElastic(meeting);

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
		meetingRepository.saveMeetingElastic(meeting);

		return new MeetingResponseDto(meeting);
	}

	@Override
	public MeetingPagingResponseDto<MeetingDocument> getMeetings(String title, MeetingStatus status,
		LocalDateTime meetingDate, Integer categoryId, int page, int size) {

		Pageable pageable = PageRequest
			.of(page - 1, size, Sort.Direction.DESC, "createdAt");

		Page<MeetingDocument> mt = meetingRepository.getMeetings(title, meetingDate, status, categoryId,
			pageable);
		List<MeetingDocument> response = mt.stream().toList();

		return new MeetingPagingResponseDto<>(
			response,
			mt.getTotalElements(),
			mt.getTotalPages(),
			mt.getNumber() + 1
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

		meetingRepository.deleteMeetingElastic(meeting);
		meeting.delete();

		eventPublisher.publishEvent(new MeetingEvents.Delete(
			meeting.getId(),
			meeting.getTitle(),
			meeting.getParticipants().stream().map(MeetingParticipant::getId).toList()
		));
	}

	@Override
	public List<MeetingResponseDto> getMeetingsByUserId(Long userId) {
		List<Meeting> meetings = meetingRepository.findMeetingsByUserId(userId);

		return meetings.stream().map(MeetingResponseDto::new).toList();
	}

	/**
	 * Meeting Participant Service
	 */

	// 낙관적 락 대신 분산락 고려중, 현재는 흐름만 구현
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

		// 참가자 수 확인
		if (meeting.getCurrentParticipantsCount() >= meeting.getMaxParticipantsCount()) {
			throw new MeetingException(MeetingExceptionCode.MEETING_IS_FULL);
		}

		// 인원 추가
		meeting.addMeetingParticipant();

		// 참가비 무료일 경우 즉시 참가자 추가
		if(meeting.getParticipationFee() == 0) {
			MeetingParticipant participant = new MeetingParticipant(meeting.getId(), userId);
			meetingRepository.saveParticipant(participant);
			meetingEventPublisher.publishParticipantEvents(
				new ParticipantEvents.Join(meetingId, userId, meeting.getHostUserId(), user.getNickname())
			);
			return new ParticipantCreateResponseDto("COMPLETE", "참가 완료");
		} else {
			meetingEventPublisher.publishParticipantEvents(new ParticipantEvents.Register(meetingId, userId));
		}

		// createParticipant 에서는 이벤트 발행 까지만 진행
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
		if (!meetingRepository.existsById(meetingId)) {
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

		meetingEventPublisher.publishParticipantEvents(
			new ParticipantEvents.Cancel(meetingId, meeting.getHostUserId(), userId, user.getNickname())
		);

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
	public ParticipantCountResponseDto getParticipantCount(Long userId, Long meetingId, Boolean attendance,
		LocalDateTime createdAt) {

		if (createdAt == null) {
			createdAt = LocalDateTime.now().minusYears(1);
		}

		Long counts = meetingRepository.countParticipants(userId, meetingId, attendance, createdAt);

		return new ParticipantCountResponseDto(userId, meetingId, counts, attendance, createdAt);
	}

	// 참가자 감소
	@Override
	public ParticipantResponseDto removeParticipant(Long meetingId, MeetingParticipant participant) {
		return participantService.removeParticipant(meetingId, participant);
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
