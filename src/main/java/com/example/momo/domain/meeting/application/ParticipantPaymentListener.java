package com.example.momo.domain.meeting.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.payment.application.PaymentService;
import com.example.momo.global.infrastructure.client.user.UserClient;
import com.example.momo.global.infrastructure.client.user.dto.UserClientResponseDto;
import com.example.momo.global.infrastructure.springEvent.MeetingEvents;
import com.example.momo.global.infrastructure.springEvent.meeting.ParticipationCancelCompletedEvents;
import com.example.momo.global.infrastructure.springEvent.meeting.ParticipationFailedEvents;
import com.example.momo.global.infrastructure.springEvent.payment.PaymentCompletedEvent;
import com.example.momo.global.infrastructure.springEvent.payment.PaymentRefundedEvent;
import com.example.momo.global.utils.RetryUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ParticipantPaymentListener {

	private final UserClient userClient;

	private final ApplicationEventPublisher eventPublisher;
	private final MeetingReader meetingReader;
	private final MeetingService meetingService;
	private final PaymentService paymentService;

	@Async
	@EventListener
	public void handlePaymentSuccessEvent(PaymentCompletedEvent event) {

		Long meetingId = event.getMeetingId();
		Long userId = event.getUserId();

		Meeting meeting = meetingReader.getMeetingById(meetingId);
		UserClientResponseDto user = userClient.getUser(userId);

		// 참가자 추가 중 예외 발생 시 환불(가능하면 결제 취소) 또는 이벤트만 발행
		try {
			RetryUtil.retry(() -> meetingService.addParticipant(meetingId, userId), 5);
			eventPublisher.publishEvent(new MeetingEvents.Join(meetingId, meeting.getHostUserId(), user.getNickname()));
		} catch (OptimisticLockingFailureException e) {
			eventPublisher.publishEvent(new ParticipationFailedEvents(
				meetingId, userId, event.getPaymentId()
			));
			throw e;
		}
	}

	// 비동기 참가 취소 환불
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleMeetingCancelEvent(PaymentRefundedEvent event) {

		Meeting meeting = meetingReader.getMeetingById(event.getMeetingId());

		eventPublisher.publishEvent(new ParticipationCancelCompletedEvents(
			event.getMeetingId(),
			meeting.getHostUserId(),
			event.getUserId()
		));
	}
}