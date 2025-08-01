package com.example.momo.domain.messagehub.application.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.momo.domain.messagehub.application.handler.EventRoutingHandler;
import com.example.momo.global.springEvent.follow.FollowMessageEvents;
import com.example.momo.global.springEvent.meeting.MeetingMessageEvents;
import com.example.momo.global.springEvent.payment.PaymentMessageEvents;

import lombok.extern.slf4j.Slf4j;

/**
 * 도메인 이벤트를 수신하여 {@link EventRoutingHandler}로 위임하는 이벤트 리스너입니다.
 *
 * <p>
 * 도메인의 이벤트를 각각 수신하여
 * 알림 처리 흐름으로 전달합니다.
 */
@Component
@Slf4j
public class DomainEventListener {

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleMeetingMessageEvent(MeetingMessageEvents.MeetingMessageEvent event) {
		log.warn("현재 사용되지 않는 이벤트 - RabbitListener 로 변경 필요");
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleFollowMessageEvent(FollowMessageEvents.FollowEvent event) {
		log.warn("현재 사용되지 않는 이벤트 - RabbitListener 로 변경 필요");

	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePaymentMessageEvent(PaymentMessageEvents.PaymentEvent event) {
		log.warn("현재 사용되지 않는 이벤트 - RabbitListener 로 변경 필요");
	}
}
