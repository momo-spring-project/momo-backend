package com.example.momo.domain.messagehub.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.momo.global.infrastructure.springEvent.MeetingEvents;
import com.example.momo.global.infrastructure.springEvent.NotificationEvent;

import lombok.RequiredArgsConstructor;

//이벤트 받아서 흐름 제어
@Component
@RequiredArgsConstructor
public class EventRoutingHandler {

	private final NotificationEventProvider notificationEventProvider;

	private final ApplicationEventPublisher eventPublisher;

	public void handleMeetingEvent(MeetingEvents.MeetingEvent event) {
		NotificationEvent notificationEvent = notificationEventProvider.processMeeting(event);

		if (notificationEvent == null) {
			return;
		}

		eventPublisher.publishEvent(notificationEvent);
	}
}
