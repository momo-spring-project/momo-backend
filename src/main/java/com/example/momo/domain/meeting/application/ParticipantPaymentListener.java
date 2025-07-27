package com.example.momo.domain.meeting.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.momo.domain.meeting.domain.MeetingRepository;
import com.example.momo.domain.payment.application.PaymentService;
import com.example.momo.global.infrastructure.client.user.UserClient;

import lombok.RequiredArgsConstructor;

/**
 ********************
 *	보험용, 사용중 x  *
 ********************
 */

@Component
@RequiredArgsConstructor
public class ParticipantPaymentListener {

	private final UserClient userClient;

	private final ApplicationEventPublisher eventPublisher;
	private final MeetingReader meetingReader;
	private final MeetingRepository meetingRepository;
	private final PaymentService paymentService;

	// 클라이언트 이용한 동기처리로 진행 예정
	// addParticipant는 MeetingServiceImpl에 있음
	// 결제 성공 어떤 이벤트인지 확인해서 넣기
	// @EventListener
	// public void handlePaymentSuccessEvent() {
	//
	// 	Long meetingId = 1L; // event.meetingId (예상)
	// 	Long userId = 1L; // event.userId (예상)
	//
	// 	Meeting meeting = meetingReader.getMeetingById(meetingId);
	// 	UserClientResponseDto user = userClient.getUser(userId);
	//
	// 	// 참가자 추가 중 예외 발생 시 환불
	// 	try {
	// 		RetryUtil.retry(() -> addParticipant(meetingId, userId), 5);
	// 		eventPublisher.publishEvent(new MeetingEvents.Join(meetingId, meeting.getHostUserId(), user.getNickname()));
	// 	} catch (OptimisticLockingFailureException e) {
	// 		paymentService.refundPayment(
	// 			1L, // event.paymentId (예상)
	// 			userId,
	// 			new RefundRequest("Join Meeting Fail")
	// 		);
	// 		throw e;
	// 	}
	// }
}