package com.example.momo.domain.messagehub.application.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.momo.domain.messagehub.application.handler.EventRoutingHandler;
import com.example.momo.global.common.aop.EventLoggable;
import com.example.momo.global.springEvent.follow.FollowMessageEvents;
import com.example.momo.global.springEvent.meeting.MeetingMessageEvents;
import com.example.momo.global.springEvent.payment.PaymentMessageEvents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 도메인 이벤트를 수신하여 {@link EventRoutingHandler}로 위임하는 이벤트 리스너입니다.
 *
 * <p>
 * 도메인의 이벤트를 각각 수신하여
 * 알림 처리 흐름으로 전달합니다.
 */
@Component
@RequiredArgsConstructor
@Async
@Slf4j
public class DomainEventListener {
	private final EventRoutingHandler eventRoutingHandler;

	@EventLoggable
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleMeetingMessageEvent(MeetingMessageEvents.MeetingMessageEvent event) {
		eventRoutingHandler.handleMeetingMessage(event);
	}

	@EventLoggable
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleFollowMessageEvent(FollowMessageEvents.FollowEvent event) {

		eventRoutingHandler.handleFollowMessage(event);
	}

	@EventLoggable
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePaymentMessageEvent(PaymentMessageEvents.PaymentEvent event) {
		eventRoutingHandler.handlePaymentMessage(event);
	}
}
