package com.example.momo.domain.eventhub.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.momo.global.infrastructure.springEvent.MeetingEvents;

import lombok.RequiredArgsConstructor;

//이벤트 받아서 흐름 제어
@Component
@RequiredArgsConstructor
public class EventRoutingHandler {

	private final NotificationEventProvider notificationEventProvider;

	private final ApplicationEventPublisher eventPublisher;

	public void handleMeetingEvent(MeetingEvents.MeetingEvent event) {
		eventPublisher.publishEvent(notificationEventProvider.processMeeting(event));
	}
}
