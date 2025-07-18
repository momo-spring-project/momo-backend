package com.example.momo.domain.notification.service;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.notification.dto.meeting.NotificationMeetingEvent;
import com.example.momo.domain.notification.entity.Notification;
import com.example.momo.domain.notification.repository.NotificationJpaRepository;
import com.example.momo.global.socket.WebSocketHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private final NotificationJpaRepository notificationJpaRepository;

	private final WebSocketHandler webSocketHandler;

	private final ApplicationContext applicationContext;

	@Override
	@Transactional
	public void processNotification(NotificationMeetingEvent command) {
		//save 와 send 모두 별도의 transaction 에서 처리하기 위해 proxy 객체 사용
		NotificationService proxy = applicationContext.getBean(NotificationService.class);
		proxy.saveNotification(command);
		proxy.sendNotification(command);

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveNotification(NotificationMeetingEvent command) {
		Notification notification = command.toEntity();

		notificationJpaRepository.save(notification);

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void sendNotification(NotificationMeetingEvent command) {
		webSocketHandler.sendToUser(command.toMessage());
	}
}
