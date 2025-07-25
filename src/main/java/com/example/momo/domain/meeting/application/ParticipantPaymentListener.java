package com.example.momo.domain.meeting.application;

import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.payment.application.PaymentService;
import com.example.momo.domain.payment.domain.dto.RefundRequest;
import com.example.momo.global.infrastructure.client.user.UserClient;
import com.example.momo.global.infrastructure.client.user.dto.UserClientResponseDto;
import com.example.momo.global.infrastructure.springEvent.MeetingEvents;
import com.example.momo.global.utils.RetryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ParticipantPaymentListener {

	private final UserClient userClient;

	private final ApplicationEventPublisher eventPublisher;
	private final MeetingReader meetingReader;
	private final MeetingService meetingService;
	private final PaymentService paymentService;

	// 결제 성공 어떤 이벤트인지 확인해서 넣기(이벤트 안넣으면 오류 떠서 임시로 넣어둠)
	@Async
	@EventListener
	public void handlePaymentSuccessEvent(MeetingEvents.Join event) {

		Long meetingId = 1L; // event.meetingId (예상)
		Long userId = 1L; // event.userId (예상)

		Meeting meeting = meetingReader.getMeetingById(meetingId);
		UserClientResponseDto user = userClient.getUser(userId);

		// 참가자 추가 중 예외 발생 시 환불
		try {
			RetryUtil.retry(() -> meetingService.addParticipant(meetingId, userId), 5);
			eventPublisher.publishEvent(new MeetingEvents.Join(meetingId, meeting.getHostUserId(), user.getNickname()));
		} catch (OptimisticLockingFailureException e) {
			paymentService.refundPayment(
				1L, // event.paymentId (예상)
				userId,
				new RefundRequest("Join Meeting Fail")
			);
			throw e;
		}
	}

	// 비동기 참가 취소 환불
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleMeetingCancelEvent(MeetingEvents.Cancel event) {
		paymentService.refundPayment(
			1L, // event.getPaymentId
			1L, // event.getUserId
			new RefundRequest("Meeting Participation Cancel")
		);
	}
}