package com.example.momo.domain.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.notification.dto.NotificationEvent;
import com.example.momo.domain.notification.entity.Notification;
import com.example.momo.domain.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private final NotificationRepository notificationRepository;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processNotification(NotificationEvent event) {
		createNotification(event);
		sendNotification(event);

	}

	@Override
	public void createNotification(NotificationEvent event) {
		Notification notification = event.toEntity();

		notificationRepository.save(notification);

	}

	@Override
	public void sendNotification(NotificationEvent event) {

	}
}
