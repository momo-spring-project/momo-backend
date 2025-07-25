package com.example.momo.domain.messagehub.application.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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
public class DomainEventListener {
	private final EventRoutingHandler eventRoutingHandler;

	@EventListener
	public void handle(MeetingEvents.MeetingEvent event) {
		eventRoutingHandler.handleMeetingEvent(event);
	}

	@EventListener
	public void handle(FollowEvents.FollowEvent event) {
		eventRoutingHandler.handleFollowEvent(event);
	}

	@EventListener
	public void handle(PaymentEvents.PaymentEvent event) {
		eventRoutingHandler.handlePaymentEvent(event);
	}
}
