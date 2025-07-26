package com.example.momo.domain.messagehub.application.handler;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.provider.NotificationEventProvider;
import com.example.momo.global.infrastructure.springEvent.MeetingEvents;
import com.example.momo.global.infrastructure.springEvent.message.FollowEvents;
import com.example.momo.global.infrastructure.springEvent.message.PaymentEvents;
import com.example.momo.global.infrastructure.springEvent.notification.NotificationEvent;

import lombok.RequiredArgsConstructor;

/**
 * 도메인 이벤트를 수신하여 알림 이벤트로 변환하고 발행하는 라우팅 핸들러입니다.
 * <p>
 * 다양한 도메인 이벤트를 받아
 * {@link NotificationEventProvider}를 통해 {@link NotificationEvent}로 변환하고,
 * Spring 의 {@link ApplicationEventPublisher}를 통해 알림 이벤트를 발행합니다.
 */
@Component
@RequiredArgsConstructor
public class EventRoutingHandler {

	private final NotificationEventProvider notificationEventProvider;

	private final ApplicationEventPublisher eventPublisher;

	public void handleMeetingEvent(MeetingEvents.MeetingEvent event) {
		NotificationEvent notificationEvent = notificationEventProvider.processMeetingMessage(event);

		if (notificationEvent == null) {
			return;
		}
		publishNotificationEvent(notificationEvent);
	}

	public void handleFollowEvent(FollowEvents.FollowEvent event) {
		NotificationEvent notificationEvent = notificationEventProvider.processFollowMessage(event);

		if (notificationEvent == null) {
			return;
		}

		publishNotificationEvent(notificationEvent);
	}

	public void handlePaymentEvent(PaymentEvents.PaymentEvent event) {
		NotificationEvent notificationEvent = notificationEventProvider.processPaymentMessage(event);

		if (notificationEvent == null) {
			return;
		}

		publishNotificationEvent(notificationEvent);
	}

	private void publishNotificationEvent(NotificationEvent notificationEvent) {

		eventPublisher.publishEvent(notificationEvent);

	}
}
