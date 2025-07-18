package com.example.momo.domain.notification.service;

import com.example.momo.domain.notification.dto.meeting.NotificationMeetingCommand;

public interface NotificationService {
	void processNotification(NotificationMeetingCommand command);

	void createNotification(NotificationMeetingCommand command);

	void sendNotification(NotificationMeetingCommand command);
}
