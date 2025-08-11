package com.example.momo.domain.meeting.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.momo.global.rabbitmq.dto.common.EventWrapper;
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
import com.example.momo.domain.meeting.domain.MeetingPaymentOutbox;
import com.example.momo.domain.meeting.domain.MeetingRepository;
import com.example.momo.domain.meeting.enums.ElasticsearchEventType;
import com.example.momo.domain.meeting.enums.MeetingStatus;
import com.example.momo.domain.meeting.enums.PaymentEventType;
import com.example.momo.domain.meeting.event.rabbitmq.producer.MeetingEventPublisher;
import com.example.momo.domain.meeting.event.rabbitmq.producer.MeetingProducer;
import com.example.momo.domain.meeting.event.springEvents.MeetingElasticEvents;
import com.example.momo.domain.meeting.exception.MeetingException;
import com.example.momo.domain.meeting.exception.MeetingExceptionCode;
import com.example.momo.global.rabbitmq.dto.meeting.ParticipantEvents;
import com.example.momo.global.rabbitmq.dto.meeting.MeetingAlarmMessages;
import com.example.momo.global.springEvent.meeting.MeetingMessageEvents;
import com.example.momo.global.utils.HaversineUtils;
import com.example.momo.global.webclient.category.CategoryClient;
import com.example.momo.global.webclient.category.dto.CategoryClientResponseDto;
import com.example.momo.global.webclient.user.UserClient;
import com.example.momo.global.webclient.user.dto.UserClientResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static com.example.momo.global.rabbitmq.constant.EventTypeNames.*;
import static com.example.momo.global.rabbitmq.constant.RoutingKeys.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

	private final ApplicationEventPublisher eventPublisher;
	private final MeetingRepository meetingRepository;
	private final UserClient userClient;
	private final CategoryClient categoryClient;
	private final MeetingProducer meetingProducer;
	private final MeetingOutboxService meetingOutboxService;
	private final MeetingEventPublisher meetingEventPublisher;
	private final RedissonClient redissonClient;
	private final MeetingPaymentOutboxService meetingPaymentOutboxService;
	private final ObjectMapper objectMapper;

	// 모임 조회
	public Meeting getMeetingById(Long meetingId) {

		Meeting meeting = meetingRepository.findById(meetingId)
			.orElseThrow(() -> new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND));
		if(meeting.getIsDeleted()){
			throw new MeetingException(MeetingExceptionCode.DELETED_MEETING);
		}

		return meeting;
	}

	// 참가자 조회
	public MeetingParticipant getParticipantByMeetingIdAndUserId(Long meetingId, Long userId) {
		Meeting meeting = getMeetingById(meetingId);

		return meeting.getParticipants().stream().filter(p -> p.getUserId().equals(userId)).findFirst()
			.orElseThrow(() -> new MeetingException(MeetingExceptionCode.PARTICIPANT_NOT_FOUND));
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

		MeetingParticipant participant = MeetingParticipant.createParticipant(meeting, userId);
		meeting.getParticipants().add(participant);

		eventPublisher.publishEvent(new MeetingElasticEvents.Save(savedMeeting, outbox.getId()));

		meetingProducer.createMeetingMQ(new MeetingAlarmMessages.Create(
			meeting.getHostUserId(),
			meeting.getId(),
			meeting.getTitle(),
			meeting.getCategoryId(),
			category.getName(),
			meeting.getLatitude(),
			meeting.getLongitude(),
			meeting.getMeetingDate()
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

		meetingProducer.updateMeetingMQ(new MeetingAlarmMessages.Update(
			meeting.getId(),
			meeting.getTitle(),
			meeting.getParticipants().stream().map(MeetingParticipant::getId).toList(),
			meeting.getMeetingDate()
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

		meetingProducer.updateMeetingMQ(new MeetingAlarmMessages.Update(
			meeting.getId(),
			meeting.getTitle(),
			meeting.getParticipants().stream().map(MeetingParticipant::getId).toList(),
			meeting.getMeetingDate()
		));

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
		List<Long> participants = meeting.getParticipants().stream().map(MeetingParticipant::getId).toList();

		// 모임 삭제 메세지 mq 발행
		meetingProducer.deleteMeetingMQ(new MeetingAlarmMessages.Delete(
			meeting.getHostUserId(),
			meeting.getId(),
			meeting.getTitle(),
			participants
		));

		// 모임 삭제시 참가자들 환불 mq 발행
		// 상태가 아직 진행중, 참가자가 존재 시 환불 이벤트 발행
		if (!meeting.getParticipants().isEmpty() &&
			meeting.getStatus().equals(MeetingStatus.IN_PROGRESS)) {

			MeetingMessageEvents.Delete deleteEvent = new MeetingMessageEvents.Delete(
				meeting.getId(),
				meeting.getTitle(),
				participants
			);

			try {
				String eventPayload = objectMapper.writeValueAsString(deleteEvent);

				EventWrapper<?> wrapper = EventWrapper.of(MEETING_DELETE, eventPayload);
				MeetingPaymentOutbox paymentOutbox =
					MeetingPaymentOutbox.create(
						MEETING_DELETE,
						meetingId,
						wrapper.uuId(),
						objectMapper.writeValueAsString(eventPayload)
					);
				meetingPaymentOutboxService.savePaymentOutbox(paymentOutbox);
			} catch (Exception e) {
				throw new MeetingException(MeetingExceptionCode.JSON_SERIALIZATION_ERROR);
			}

			eventPublisher.publishEvent(deleteEvent);
		}

		MeetingElasticsearchOutbox elasticsearchOutbox = new MeetingElasticsearchOutbox(meeting.getId(),
			ElasticsearchEventType.DELETE);
		meetingOutboxService.saveMeetingOutbox(elasticsearchOutbox);

		eventPublisher.publishEvent(new MeetingElasticEvents.Delete(meeting, elasticsearchOutbox.getId()));
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

		RLock lock = redissonClient.getLock("lock:meeting:" + meetingId);

		String message = "";
		String status = "";

		// (분산락) 인원 추가
		boolean locked = false;
		try {
			locked = lock.tryLock(3, 10, TimeUnit.SECONDS);

			if (!locked) {
				log.info("Lock acquire failed");
			}
			log.info("Lock acquired");

			Meeting meeting = getMeetingById(meetingId);

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
			meeting.addMeetingParticipant();

			boolean success;
			// 참가비 무료일 경우 즉시 참가자 추가
			if (meeting.getParticipationFee() == 0) {
				MeetingParticipant participant = MeetingParticipant.createParticipant(meeting, userId);

				meeting.getParticipants().add(participant);
				// 참가 완료 이벤트 발행
				success = meetingEventPublisher.publishWithConfirmParticipantEvents(
					new ParticipantEvents.Join(meetingId, userId, meeting.getHostUserId(), user.getNickname()),
					MEETING_PARTICIPANT_JOIN,
					PARTICIPANT_JOIN_KEY
				);
				if (success) {
					status = "COMPLETED";
					message = "참가 완료";
				}
			} else {
				// 참가 신청 이벤트 발행
				success = meetingEventPublisher.publishWithConfirmParticipantEvents(
					new ParticipantEvents.Register(meetingId, userId),
					MEETING_PARTICIPANT_REGISTER,
					PARTICIPANT_REGISTER_KEY
				);
				if (success) {
					status = "PENDING";
					message = "결제 요청 완료";
				}
			}

			if (!success) {
				throw new RuntimeException("Message publishing failed");
			}

			// 트랜잭션 커밋 이후에 락 해제
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					lock.unlock();
				}

				// 롤백, 커밋 실패한 경우 락 해제
				@Override
				public void afterCompletion(int status) {
					if (status != STATUS_COMMITTED && lock.isHeldByCurrentThread()) {
						lock.unlock();
					}
				}
			});
		} catch (InterruptedException e) {
			// 인터럽트 발생 시 예외 발생 명시
			Thread.currentThread().interrupt();
			throw new RuntimeException("Thread interrupted while acquiring lock", e);
		} catch (Exception e) {
			// 예외 발생 시 락 직접 해제
			if (locked && lock.isHeldByCurrentThread()) {
				lock.unlock();
				log.info("Unlocked");
				throw e;
			}
		}

		// createParticipant 에서는 이벤트 발행 까지만 진행
		return new ParticipantCreateResponseDto(status, message);
	}

	@Override
	@Transactional(readOnly = true)
	public ParticipantResponseDto getParticipant(Long meetingId, Long userId) {

		Meeting meeting = getMeetingById(meetingId);

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

		Meeting meeting = getMeetingById(meetingId);

		List<MeetingParticipant> participants = meeting.getParticipants();

		return participants.stream()
			.map(ParticipantResponseDto::new)
			.toList();
	}

	@Override
	@Transactional
	public ParticipantResponseDto deleteParticipant(Long userId, Long meetingId) {

		Meeting meeting = getMeetingById(meetingId);
		if (meeting.getHostUserId().equals(userId)) {
			throw new MeetingException(MeetingExceptionCode.HOST_CANCEL_FORBIDDEN);
		}
		isMeetingOpen(meeting);

		UserClientResponseDto user = userClient.getUser(userId);

		MeetingParticipant participant = getParticipantByMeetingIdAndUserId(meetingId, userId);

		// 6시간 전이면 취소/환불 불가
		if (LocalDateTime.now().isAfter(meeting.getMeetingDate().minusHours(6))) {
			throw new MeetingException(MeetingExceptionCode.MEETING_TIME_FORBIDDEN);
		}

		// 인원 감소, 참가자 삭제
		meeting.removeMeetingParticipant();
		meeting.getParticipants().remove(participant);

		// 참가비 있을 경우만 환불 요청 이벤트
		boolean success;
		if (meeting.getParticipationFee() == 0) {
			success = meetingEventPublisher.publishWithConfirmParticipantEvents(
				new ParticipantEvents.Cancel(
					meetingId,
					userId,
					meeting.getHostUserId(),
					user.getNickname(),
					false,
					0),
				MEETING_PARTICIPANT_CANCEL,
				PARTICIPANT_CANCEL_KEY
			);
		} else {
			success = meetingEventPublisher.publishWithConfirmParticipantEvents(
				new ParticipantEvents.Cancel(
					meetingId,
					userId,
					meeting.getHostUserId(),
					user.getNickname(),
					true,
					meeting.getParticipationFee()
				),
				MEETING_PARTICIPANT_CANCEL,
				PARTICIPANT_CANCEL_KEY
			);
		}
		if (!success) {
			throw new RuntimeException("Message publishing failed");
		}

		return new ParticipantResponseDto(participant);
	}

	@Override
	@Transactional
	public ParticipantResponseDto updateParticipantStatus(Long userId, Long meetingId, double lat, double lng) {

		Meeting meeting = getMeetingById(meetingId);

		// 모임 활성화 확인
		isMeetingOpen(meeting);

		Double destLat = meeting.getLatitude();
		Double destLng = meeting.getLongitude();

		// 위치가 목적지 근처인지 확인
		if (!HaversineUtils.isInDistance(destLat, destLng, lat, lng, 10000000)) {
			throw new MeetingException(MeetingExceptionCode.FAR_FROM_MEETING);
		}

		MeetingParticipant participant = getParticipantByMeetingIdAndUserId(meetingId, userId);

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
