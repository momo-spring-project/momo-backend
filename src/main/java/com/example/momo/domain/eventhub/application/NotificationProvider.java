package com.example.momo.domain.eventhub.application;

import org.springframework.stereotype.Component;

import com.example.momo.global.infrastructure.springEvent.MeetingEvents;
import com.example.momo.global.infrastructure.springEvent.NotificationEvent;

//메세지 분기 처리
@Component
public class NotificationProvider {
	public NotificationEvent provider(MeetingEvents.MeetingEvent event) {

		return null;
	}
}
