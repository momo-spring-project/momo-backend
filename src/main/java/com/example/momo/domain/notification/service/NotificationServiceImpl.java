package com.example.momo.domain.notification.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.notification.dto.meeting.NotificationMeetingCommand;
import com.example.momo.domain.notification.entity.Notification;
import com.example.momo.domain.notification.repository.NotificationRepository;
import com.example.momo.global.websocket.WebSocketHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private final NotificationRepository notificationRepository;

	private final WebSocketHandler webSocketHandler;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processNotification(NotificationMeetingCommand command) {
		createNotification(command);
		sendNotification(command);

	}

	@Override
	public void createNotification(NotificationMeetingCommand command) {
		Notification notification = command.toEntity();

		notificationRepository.save(notification);

	}

	@Override
	public void sendNotification(NotificationMeetingCommand command) {
		try {
			webSocketHandler.sendToUser(command.userId(), command.content());
		} catch (IOException e) {
			// fallback: 로그 or DB 저장
		}
	}
}
