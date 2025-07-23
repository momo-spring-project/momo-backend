package com.example.momo.domain.meeting.application;

import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.domain.MeetingParticipant;
import com.example.momo.domain.meeting.domain.MeetingRepository;
import com.example.momo.domain.meeting.domain.dto.response.ParticipantResponseDto;
import com.example.momo.domain.meeting.exception.MeetingException;
import com.example.momo.domain.meeting.exception.MeetingExceptionCode;
import com.example.momo.global.utils.RetryUtil;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ParticipantPaymentListener {

	private final ApplicationEventPublisher eventPublisher;
	private final MeetingReader meetingReader;
	private final MeetingRepository meetingRepository;

	private final EntityManager em;

	// 결제 성공 어떤 이벤트인지 확인해서 넣기
	@EventListener
	public void handlePaymentSuccessEvent() {

		Long meetingId = 1L;
		Long userId = 1L;

		// 참가자 추가 중 예외 발생 시 환불
		try {
			RetryUtil.retry(() -> addParticipant(meetingId, userId), 5);
			eventPublisher.publishEvent(new ParticipationSuccessEvent(meetingId, userId));
		} catch (OptimisticLockingFailureException e) {
			// 환불 알고리즘
			throw e;
		}
	}

	// 모임 참가 취소 후 환불 진행
	@EventListener
	public void handleParticipationCancelEvent(ParticipationCanceledEvent event) {
		Long meetingId = event.getMeetingId();
		Long userId = event.getUserId();

		// 환불 알고리즘
	}

	// 환불 성공 어떤 이벤트인지 확인해서 넣기
	// 환불 종류 구별 필요
	@EventListener
	public void handleRefundSuccessEvent() {

	}

	// 참가자 추가
	@Transactional
	public ParticipantResponseDto addParticipant(Long meetingId, Long userId) {

		Meeting meeting = meetingReader.getMeetingById(meetingId);

		if(meeting.getCurrentParticipantsCount() >= meeting.getMaxParticipantsCount()) {
			throw new MeetingException(MeetingExceptionCode.MEETING_IS_FULL);
		}

		// 참가자 추가
		MeetingParticipant participant = new MeetingParticipant(meeting.getId(), userId);
		MeetingParticipant savedParticipant = meetingRepository.saveParticipant(participant);

		meeting.addMeetingParticipant();

		return new ParticipantResponseDto(savedParticipant);
	}
}