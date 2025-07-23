package com.example.momo.domain.eventhub.application;

import org.springframework.stereotype.Component;

import com.example.momo.global.common.event.MeetingEvents;

//이벤트 받아서 흐름 제어
@Component
public class NotificationEventHandler {
	public void handleMeetingEvent(MeetingEvents.MeetingEvent event) {
	}
}
