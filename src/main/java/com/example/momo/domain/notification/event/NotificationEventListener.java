package com.example.momo.domain.notification.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.momo.domain.notification.dto.meeting.NotificationMeetingCommand;
import com.example.momo.domain.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

	private final NotificationService notificationService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleMeetingNotification(NotificationMeetingCommand command) {
		notificationService.processNotification(command);
	}
}
