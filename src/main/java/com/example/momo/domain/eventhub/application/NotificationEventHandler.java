package com.example.momo.domain.eventhub.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.momo.global.common.event.MeetingEvents;
import com.example.momo.global.common.event.NotificationEvent;

import lombok.RequiredArgsConstructor;

//이벤트 받아서 흐름 제어
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

	private final NotificationProvider notificationProvider;

	private final ApplicationEventPublisher eventPublisher;

	public void handleMeetingEvent(MeetingEvents.MeetingEvent event) {
		NotificationEvent notificationEvent = notificationProvider.provider(event);

		eventPublisher.publishEvent(notificationEvent);
	}
}
