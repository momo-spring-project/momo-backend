package com.example.momo.domain.eventhub.application;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.momo.global.common.event.MeetingEvents;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventHubListener {
	private final NotificationEventHandler notificationEventHandler;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(MeetingEvents.MeetingEvent event) {
		notificationEventHandler.handleMeetingEvent(event);
	}
}
