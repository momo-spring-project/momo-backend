package com.example.momo.domain.meetings.application;

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
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
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
	public ParticipantResponseDto addParticipant(Long userId, Long meetingId) {

		Meeting meeting = meetingRepository.findById(meetingId)
			.orElseThrow(() -> new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND));

		// Todo 구조 전환시 유저 조회 예외처리 수정 또는 제거 필요함
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new RuntimeException("user not found"));

		if(!meetingParticipantRepository.existsByMeetingIdAndUserId(meetingId, userId)) {
			throw new MeetingException(MeetingExceptionCode.ALREADY_PARTICIPATED);
		}

		// 모임 활성화 확인
		if(!isMeetingOpen(meeting)) {
			throw new MeetingException(MeetingExceptionCode.MEETING_IS_UNAVAILABLE);
		}

		// 유저 자격 확인
		if(user.getScore() < meeting.getMinEnterScore()) {
			throw new MeetingException(MeetingExceptionCode.INSUFFICIENT_SCORE);
		}

		//결제 알고리즘

		// Todo 동시성 처리 필요 ( 낙관적 락 고려중 )
		// 최대 인원 넘으면 예외처리
		if(meeting.getParticipants().size() >= meeting.getMaxParticipantsCount()) {
			// 환불 알고리즘
			throw new RuntimeException("Meeting is full");
		}

		MeetingParticipant participant = new MeetingParticipant(meetingId, userId);

		MeetingParticipant savedParticipant = meetingParticipantRepository.save(participant);

		return new ParticipantResponseDto(savedParticipant);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Long> getParticipants(Long meetingId) {
		return meetingParticipantRepository.findParticipantsIdsByMeetingId(meetingId);
	}

	@Override
	@Transactional
	public ParticipantResponseDto cancelParticipant(Long userId, Long meetingId) {

		MeetingParticipant participant = meetingParticipantRepository.findByMeetingIdAndUserId(meetingId, userId)
			.orElseThrow(() -> new MeetingException(MeetingExceptionCode.PARTICIPANT_NOT_FOUND));

		em.remove(participant);

		return new ParticipantResponseDto(participant);
	}

	// 모임이 활성화 되어있는 상태인지 확인
	private boolean isMeetingOpen(Meeting meeting) {

		// 시작 시간 넘김
		if(LocalDateTime.now().isAfter(meeting.getMeetingDate())) {
			return false;
		}

		// 모임 상태 FINISHED
		if(meeting.getStatus() == MeetingStatus.FINISHED) {
			return false;
		}

		// 삭제된 모임
		if(meeting.getIsDeleted()) {
			return false;
		}

		return true;
	}
}