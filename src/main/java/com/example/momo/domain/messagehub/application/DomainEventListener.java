package com.example.momo.domain.messagehub.application;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.momo.global.infrastructure.springEvent.MeetingEvents;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DomainEventListener {
	private final EventRoutingHandler eventRoutingHandler;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(MeetingEvents.MeetingEvent event) {
		eventRoutingHandler.handleMeetingEvent(event);
	}
}
