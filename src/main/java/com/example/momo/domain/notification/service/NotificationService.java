package com.example.momo.domain.notification.service;

import com.example.momo.domain.notification.dto.NotificationEvent;

public interface NotificationService {
	void processNotification(NotificationEvent event);

	void createNotification(NotificationEvent event);

	void sendNotification(NotificationEvent event);
}
