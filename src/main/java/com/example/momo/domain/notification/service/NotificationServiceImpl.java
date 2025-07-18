package com.example.momo.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.notification.dto.NotificationResponse;
import com.example.momo.domain.notification.entity.Notification;
import com.example.momo.domain.notification.repository.NotificationJpaRepository;
import com.example.momo.global.event.NotificationMeetingEvent;
import com.example.momo.global.socket.service.NotificationSender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

	private final NotificationJpaRepository notificationJpaRepository;

	private final NotificationSender notificationSender;

	@Override
	@Transactional
	public void processNotification(NotificationMeetingEvent command) {

		saveNotification(command);
		sendNotification(command);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveNotification(NotificationMeetingEvent command) {
		Notification notification = command.toEntity();

		notificationJpaRepository.save(notification);
	}

	@Override
	public List<NotificationResponse> getNotifications(Long userId) {

		return notificationJpaRepository.findAllByUserId(userId);
	}

	@Override
	public void sendNotification(NotificationMeetingEvent command) {
		notificationSender.send(command.toMessage());
	}
}
