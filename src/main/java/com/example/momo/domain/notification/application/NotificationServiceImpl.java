package com.example.momo.domain.notification.application;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.notification.domain.NotificationResponse;
import com.example.momo.domain.notification.infra.NotificationRepository;
import com.example.momo.global.event.NotificationMeetingEvent;
import com.example.momo.global.socket.service.NotificationSender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

	private final NotificationRepository notificationRepository;

	private final NotificationSender notificationSender;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveNotification(NotificationMeetingEvent event) {
		try {
			notificationRepository.save(event.toEntity());
		} catch (DataIntegrityViolationException e) {
			log.warn("DB 저장 실패 - 무결성 오류: {}", e.getMessage());
		} catch (Exception e) {
			log.error("알림 저장 실패: {}", e.getMessage(), e);
		}
	}

	@Override
	public List<NotificationResponse> getNotifications(Long userId) {

		return notificationRepository.findAllByUserId(userId).stream()
			.map(NotificationResponse::from)
			.toList();
	}

	@Override
	public void sendNotification(NotificationMeetingEvent event) {

		notificationSender.send(event.toMessage());
	}
}
