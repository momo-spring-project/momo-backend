package com.example.momo.domain.messagehub.application.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.momo.domain.messagehub.application.handler.EventRoutingHandler;
import com.example.momo.global.infrastructure.springEvent.MeetingEvents;
import com.example.momo.global.infrastructure.springEvent.message.FollowEvents;
import com.example.momo.global.infrastructure.springEvent.message.PaymentEvents;

import lombok.RequiredArgsConstructor;

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
public class DomainEventListener {
	private final EventRoutingHandler eventRoutingHandler;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(MeetingEvents.MeetingEvent event) {
		eventRoutingHandler.handleMeetingEvent(event);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(FollowEvents.FollowEvent event) {
		eventRoutingHandler.handleFollowEvent(event);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(PaymentEvents.PaymentEvent event) {
		eventRoutingHandler.handlePaymentEvent(event);
	}
}
