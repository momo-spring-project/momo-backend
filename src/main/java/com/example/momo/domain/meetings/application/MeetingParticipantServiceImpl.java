package com.example.momo.domain.meetings.application;

import com.example.momo.domain.meetings.Haversine;
import com.example.momo.domain.meetings.domain.Meeting;
import com.example.momo.domain.meetings.domain.MeetingParticipant;
import com.example.momo.domain.meetings.domain.MeetingRepository;
import com.example.momo.domain.meetings.enums.MeetingStatus;
import com.example.momo.domain.meetings.exception.MeetingException;
import com.example.momo.domain.meetings.exception.MeetingExceptionCode;
import com.example.momo.domain.meetings.domain.MeetingParticipantRepository;
import com.example.momo.domain.meetings.presentation.dto.ParticipantResponseDto;
import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.infra.UserRepository;
import com.example.momo.global.utils.RetryUtil;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingParticipantServiceImpl implements MeetingParticipantService {

	private final UserRepository userRepository;
	private final MeetingRepository meetingRepository;
	private final MeetingParticipantRepository meetingParticipantRepository;

	private final EntityManager em;

	@Override
	@Transactional
	public ParticipantResponseDto registerParticipant(Long userId, Long meetingId) {

		Meeting meeting = meetingRepository.findById(meetingId)
			.orElseThrow(() -> new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND));

		// Todo 구조 전환시 유저 조회 예외처리 수정 또는 제거 필요함
		User user = userRepository.findByIdAndIsDeletedFalse(userId)
			.orElseThrow(() -> new RuntimeException("user not found"));

		// 이미 참가했으면 예외처리
		if(meetingParticipantRepository.existsByMeetingIdAndUserId(meetingId, userId)) {
			throw new MeetingException(MeetingExceptionCode.ALREADY_PARTICIPATED);
		}

		// 모임 활성화 확인
		isMeetingOpen(meeting);

		// 유저 자격 확인
		if(user.getScore() < meeting.getMinEnterScore()) {
			throw new MeetingException(MeetingExceptionCode.INSUFFICIENT_SCORE);
		}

		//결제 알고리즘

		// 참가자 추가 중 예외 발생 시 환불
		try {
			return RetryUtil.retry(() -> addParticipant(meeting, user), 5);
		} catch (OptimisticLockingFailureException e) {
			// 환불 알고리즘
			throw e;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<Long> getParticipants(Long meetingId) {
		if(!meetingRepository.existsById(meetingId)) {
			throw new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND);
		}

		return meetingParticipantRepository.findParticipantsIdsByMeetingId(meetingId);
	}

	@Override
	@Transactional
	public ParticipantResponseDto cancelParticipant(Long userId, Long meetingId) {

		Meeting meeting = meetingRepository.findById(meetingId)
			.orElseThrow(() -> new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND));

		User user = userRepository.findByIdAndIsDeletedFalse(userId)
			.orElseThrow(() -> new RuntimeException("user not found"));

		ParticipantResponseDto responseDto = RetryUtil.retry(() -> subParticipant(meeting, user), 5);

		// 환불 알고리즘

		return responseDto;
	}

	@Override
	@Transactional
	public ParticipantResponseDto updateParticipantStatus(Long userId, Long meetingId, double lat, double lng) {

		Meeting meeting = meetingRepository.findById(meetingId)
			.orElseThrow(() -> new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND));

		// 모임 활성화 확인
		isMeetingOpen(meeting);

		Double destLat = meeting.getLatitude();
		Double destLng = meeting.getLongitude();

		// 위치가 목적지 근처인지 확인
		if(!Haversine.inDistance(destLat, destLng, lat, lng)) {
			throw new MeetingException(MeetingExceptionCode.FAR_FROM_MEETING);
		}

		MeetingParticipant participant = meetingParticipantRepository.findByMeetingIdAndUserId(meetingId, userId)
			.orElseThrow(() -> new MeetingException(MeetingExceptionCode.PARTICIPANT_NOT_FOUND));

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

	// 참가자 추가
	@Transactional
	protected ParticipantResponseDto addParticipant(Meeting meeting, User user) {

		if(meeting.getCurrentParticipantsCount() >= meeting.getMaxParticipantsCount()) {
			throw new MeetingException(MeetingExceptionCode.MEETING_IS_FULL);
		}

		// 참가자 추가
		MeetingParticipant participant = new MeetingParticipant(meeting.getId(), user.getId());
		MeetingParticipant savedParticipant = meetingParticipantRepository.save(participant);

		// 인원 계산
		meeting.addMeetingParticipant();

		return new ParticipantResponseDto(savedParticipant);
	}

	// 참가자 감소
	@Transactional
	protected ParticipantResponseDto subParticipant(Meeting meeting, User user) {

		if(meeting.getCurrentParticipantsCount() <= 0) {
			throw new MeetingException(MeetingExceptionCode.INVALID_PARTICIPANT_COUNT);
		}

		MeetingParticipant participant = meetingParticipantRepository
			.findByMeetingIdAndUserId(meeting.getId(), user.getId())
			.orElseThrow(() -> new MeetingException(MeetingExceptionCode.PARTICIPANT_NOT_FOUND));

		// 인원 계산, 참가자 삭제
		meeting.removeMeetingParticipant();
		em.remove(participant);

		return new ParticipantResponseDto(participant);
	}
}