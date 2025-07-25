package com.example.momo.domain.messagehub.application;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.momo.global.infrastructure.springEvent.MeetingEvents;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DomainEventListener {
	private final EventRoutingHandler eventRoutingHandler;

	@EventListener
	public void handle(MeetingEvents.MeetingEvent event) {
		eventRoutingHandler.handleMeetingEvent(event);
	}
}
