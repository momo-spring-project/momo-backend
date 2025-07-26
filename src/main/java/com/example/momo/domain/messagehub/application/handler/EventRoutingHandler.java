package com.example.momo.domain.messagehub.application.handler;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.provider.NotificationEventProvider;
import com.example.momo.global.infrastructure.springEvent.MeetingEvents;
import com.example.momo.global.infrastructure.springEvent.follow.FollowMessageEvents;
import com.example.momo.global.infrastructure.springEvent.notification.MessageEvents;
import com.example.momo.global.infrastructure.springEvent.payment.PaymentMessageEvents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 도메인 이벤트를 수신하여 알림 이벤트로 변환하고 발행하는 라우팅 핸들러입니다.
 * <p>
 * 다양한 도메인 이벤트를 받아
 * {@link NotificationEventProvider}를 통해 {@link MessageEvents}로 변환하고,
 * Spring 의 {@link ApplicationEventPublisher}를 통해 알림 이벤트를 발행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventRoutingHandler {

	private final NotificationEventProvider notificationEventProvider;

	private final ApplicationEventPublisher eventPublisher;

	public void handleMeetingMessage(MeetingEvents.MeetingEvent event) {
		MessageEvents messageEvents = notificationEventProvider.processMeetingMessage(event);

		if (!hasMessageEvent(messageEvents)) {
			return;
		}

		publishMessageEvent(messageEvents);
	}

	public void handleFollowMessage(FollowMessageEvents.FollowEvent event) {
		MessageEvents messageEvents = notificationEventProvider.processFollowMessage(event);

		if (!hasMessageEvent(messageEvents)) {
			return;
		}

		publishMessageEvent(messageEvents);
	}

	public void handlePaymentMessage(PaymentMessageEvents.PaymentEvent event) {
		MessageEvents messageEvents = notificationEventProvider.processPaymentMessage(event);

		if (!hasMessageEvent(messageEvents)) {
			return;
		}

		publishMessageEvent(messageEvents);
	}

	private boolean hasMessageEvent(MessageEvents messageEvents) {
		if (messageEvents == null) {
			log.warn("MessageEvent 발행 시도 - 이벤트가 null입니다");
			return false;
		}
		return true;
	}

	private void publishMessageEvent(MessageEvents messageEvents) {

		eventPublisher.publishEvent(messageEvents);

	}
}
