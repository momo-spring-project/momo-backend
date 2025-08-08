package com.example.momo.domain.meeting.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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
import com.example.momo.domain.meeting.domain.MeetingElasticsearchOutbox;
import com.example.momo.domain.meeting.domain.MeetingParticipant;
import com.example.momo.domain.meeting.domain.MeetingRepository;
import com.example.momo.domain.meeting.enums.ElasticsearchEventType;
import com.example.momo.domain.meeting.enums.MeetingStatus;
import com.example.momo.domain.meeting.event.rabbitmq.producer.MeetingEventPublisher;
import com.example.momo.domain.meeting.event.rabbitmq.producer.MeetingProducer;
import com.example.momo.domain.meeting.event.springEvents.MeetingElasticEvents;
import com.example.momo.domain.meeting.exception.MeetingException;
import com.example.momo.domain.meeting.exception.MeetingExceptionCode;
import com.example.momo.global.rabbitmq.dto.ParticipantEvents;
import com.example.momo.global.springEvent.meeting.MeetingMessageEvents;
import com.example.momo.global.utils.HaversineUtils;
import com.example.momo.global.webclient.category.CategoryClient;
import com.example.momo.global.webclient.category.dto.CategoryClientResponseDto;
import com.example.momo.global.webclient.user.UserClient;
import com.example.momo.global.webclient.user.dto.UserClientResponseDto;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

	private final ApplicationEventPublisher eventPublisher;
	private final MeetingRepository meetingRepository;
	private final MeetingReader meetingReader;
	private final UserClient userClient;
	private final CategoryClient categoryClient;
	private final MeetingProducer meetingProducer;
	private final MeetingOutboxService meetingOutboxService;
	private final MeetingEventPublisher meetingEventPublisher;
	private final EntityManager entityManager;
	private final RedissonClient redissonClient;

	@Override
	public Meeting getMeetingEntity(Long meetingId) {
		return meetingRepository.findById(meetingId).orElseThrow(() ->
			new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND));
	}

	@Override
	@Transactional
	public MeetingResponseDto createMeeting(MeetingCreateRequestDto request, Long userId) {

		CategoryClientResponseDto category = categoryClient.getCategory(request.getCategoryId());

		Meeting meeting = request.toMeeting(userId);
		Meeting savedMeeting = meetingRepository.save(meeting);

		MeetingElasticsearchOutbox outbox = new MeetingElasticsearchOutbox(savedMeeting.getId(),
			ElasticsearchEventType.SAVE);
		meetingOutboxService.saveMeetingOutbox(outbox);

		MeetingParticipant participant = MeetingParticipant.createParticipant(meeting.getId(), userId);
		meeting.getParticipants().add(participant);

		eventPublisher.publishEvent(new MeetingElasticEvents.Save(savedMeeting, outbox.getId()));

		// TODO : 기존 DTO 사용중이므로 메세지허브로 보내는 DTO 지운님 PR 병합 이후 수정 예정
		meetingProducer.createMeetingMQ(new MeetingMessageEvents.Create(
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

		MeetingElasticsearchOutbox outbox = new MeetingElasticsearchOutbox(meeting.getId(),
			ElasticsearchEventType.SAVE);
		meetingOutboxService.saveMeetingOutbox(outbox);

		meeting.updateMeeting(request);
		eventPublisher.publishEvent(new MeetingElasticEvents.Save(meeting, outbox.getId()));

		meetingProducer.updateMeetingMQ(new MeetingMessageEvents.Update(
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

		MeetingElasticsearchOutbox outbox = new MeetingElasticsearchOutbox(meeting.getId(),
			ElasticsearchEventType.SAVE);
		meetingOutboxService.saveMeetingOutbox(outbox);

		meeting.updateStatus(status);
		eventPublisher.publishEvent(new MeetingElasticEvents.Save(meeting, outbox.getId()));

		return new MeetingResponseDto(meeting);
	}

	@Override
	public MeetingPagingResponseDto<MeetingDocument> getMeetings(String title, MeetingStatus status,
		LocalDateTime meetingDate, Integer categoryId, int page, int size) {

		Pageable pageable = PageRequest
			.of(page - 1, size, Sort.Direction.DESC, "createdAt");

		Page<MeetingDocument> meetings;

		try {
			meetings = meetingRepository.getMeetings(title, meetingDate, status, categoryId,
				pageable);
		} catch (Exception e) {
			Page<Meeting> meetingPage = meetingRepository.getMeetingsForDatabase(title, meetingDate, status, categoryId,
				pageable);
			meetings = meetingPage.map(MeetingDocument::from);
		}

		List<MeetingDocument> response = meetings.stream().toList();

		return new MeetingPagingResponseDto<>(
			response,
			meetings.getTotalElements(),
			meetings.getTotalPages(),
			meetings.getNumber() + 1
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

		meetingProducer.deleteMeetingMQ(new MeetingMessageEvents.Delete(
			meeting.getId(),
			meeting.getTitle(),
			meeting.getParticipants().stream().map(MeetingParticipant::getId).toList()
		));

		MeetingElasticsearchOutbox outbox = new MeetingElasticsearchOutbox(meeting.getId(),
			ElasticsearchEventType.DELETE);
		meetingOutboxService.saveMeetingOutbox(outbox);

		eventPublisher.publishEvent(new MeetingElasticEvents.Delete(meeting, outbox.getId()));
	}

	@Override
	public List<MeetingResponseDto> getMeetingsByUserId(Long userId) {
		List<Meeting> meetings = meetingRepository.findMeetingsByUserId(userId);

		return meetings.stream().map(MeetingResponseDto::new).toList();
	}

	/**
	 * 스케줄러 es 저장 메서드
	 */
	@Override
	public void createElasticMeeting(Meeting meeting) {
		meetingRepository.saveMeetingElastic(meeting);
	}

	/**
	 * 스케줄러 es 삭제 메서드
	 */
	@Override
	public void deleteElasticMeeting(Meeting meeting) {
		meetingRepository.deleteMeetingElastic(meeting);
	}

	/**
	 * Meeting Participant Service
	 */

	@Override
	@Transactional
	public ParticipantCreateResponseDto createParticipant(Long userId, Long meetingId) {
		RLock lock = redissonClient.getLock("lock:meeting:free:" + meetingId);

		Meeting meeting = meetingReader.getMeetingById(meetingId);
;
		// 이미 참가했으면 예외처리
		if (meeting.getParticipants()
			.stream()
			.anyMatch(p -> p.getUserId().equals(userId))
		) {
			throw new MeetingException(MeetingExceptionCode.ALREADY_PARTICIPATED);
		}

		// 모임 활성화 확인
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

		String status;
		String message;
		boolean success;
		// 참가비 무료일 경우 즉시 참가자 추가
		if (meeting.getParticipationFee() == 0) {
			MeetingParticipant participant = MeetingParticipant.createParticipant(meeting.getId(), userId);

			boolean locked = false;
			try {
				locked = lock.tryLock(3, 10, TimeUnit.SECONDS);

				if (locked) {
					log.info("Lock acquired");
					meeting.addMeetingParticipant();
					entityManager.flush();
				} else {
					log.info("Lock acquire failed");
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("Thread interrupted while acquiring lock", e);
			} finally {
				if (locked && lock.isHeldByCurrentThread()) {
					lock.unlock();
					log.info("Unlocked");
				}
			}
			meeting.getParticipants().add(participant);

			success = meetingEventPublisher.publishWithConfirmParticipantEvents(
				new ParticipantEvents.Join(meetingId, userId, meeting.getHostUserId(), user.getNickname())
			);

			status = success ? "COMPLETED" : "FAILED";
			message = success ? "참가 완료" : "요청 실패";
		} else {
			success = meetingEventPublisher.publishWithConfirmParticipantEvents(
				new ParticipantEvents.Register(meetingId, userId)
			);

			status = success ? "PENDING" : "FAILED";
			message = success ? "결제 대기" : "요청 실패";
		}

		// createParticipant 에서는 이벤트 발행 까지만 진행
		return new ParticipantCreateResponseDto(status, message);
	}

	@Override
	@Transactional(readOnly = true)
	public ParticipantResponseDto getParticipant(Long meetingId, Long userId) {

		Meeting meeting = meetingReader.getMeetingById(meetingId);

		MeetingParticipant participant = meeting.getParticipants()
			.stream()
			.filter(p -> p.getUserId().equals(userId))
			.findFirst()
			.orElseThrow(() -> new MeetingException(MeetingExceptionCode.PARTICIPANT_NOT_FOUND));

		return new ParticipantResponseDto(participant);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ParticipantResponseDto> getParticipants(Long meetingId) {

		Meeting meeting = meetingReader.getMeetingById(meetingId);

		List<MeetingParticipant> participants = meeting.getParticipants();

		return participants.stream()
			.map(ParticipantResponseDto::new)
			.toList();
	}

	@Override
	@Transactional
	public ParticipantResponseDto deleteParticipant(Long userId, Long meetingId) {

		Meeting meeting = meetingReader.getMeetingById(meetingId);
		if (meeting.getHostUserId().equals(userId)) {
			throw new MeetingException(MeetingExceptionCode.HOST_CANCEL_FORBIDDEN);
		}
		isMeetingOpen(meeting);

		UserClientResponseDto user = userClient.getUser(userId);

		MeetingParticipant participant = meetingReader.getParticipantByMeetingIdAndUserId(meetingId, userId);

		// 6시간 전이면 취소/환불 불가
		if (LocalDateTime.now().isAfter(meeting.getMeetingDate().minusHours(6))) {
			throw new MeetingException(MeetingExceptionCode.MEETING_TIME_FORBIDDEN);
		}

		// 인원 감소, 참가자 삭제
		meeting.removeMeetingParticipant();
		meetingRepository.removeParticipant(participant.getId());

		// 참가비 있을 경우만 환불 요청 이벤트
		if (meeting.getParticipationFee() == 0) {
			meetingEventPublisher.publishParticipantEvents(
				new ParticipantEvents.Cancel(
					meetingId,
					userId,
					meeting.getHostUserId(),
					user.getNickname(),
					false,
					0)
			);
		} else {
			meetingEventPublisher.publishParticipantEvents(
				new ParticipantEvents.Cancel(
					meetingId,
					userId,
					meeting.getHostUserId(),
					user.getNickname(),
					true,
					meeting.getParticipationFee()
				)
			);
		}

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
		if (!HaversineUtils.isInDistance(destLat, destLng, lat, lng, 10000000)) {
			throw new MeetingException(MeetingExceptionCode.FAR_FROM_MEETING);
		}

		MeetingParticipant participant = meetingReader.getParticipantByMeetingIdAndUserId(meetingId, userId);

		if (participant.getAttendanceStatus()) {
			throw new MeetingException(MeetingExceptionCode.ALREADY_ATTENDED);
		}

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
