package com.example.momo.domain.notification.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.notification.domain.NotificationRepository;
import com.example.momo.domain.notification.domain.dto.NotificationDto;
import com.example.momo.domain.notification.domain.dto.NotificationResponseDto;
import com.example.momo.global.socket.dto.WebSocketNotificationDto;
import com.example.momo.global.socket.service.WebSocketNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

	private final NotificationRepository notificationRepository;

	private final WebSocketNotificationService webSocketNotificationService;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void createNotification(NotificationDto dto) {
		try {
			notificationRepository.save(dto.toEntity());
		} catch (DataIntegrityViolationException e) {
			log.warn("DB 저장 실패 - 무결성 오류: {}", e.getMessage());
		} catch (Exception e) {
			log.error("알림 저장 실패: {}", e.getMessage(), e);
		}
	}

	@Override
	public List<NotificationResponseDto> getNotifications(Long userId) {

		return notificationRepository.findAllByUserId(userId).stream()
			.map(NotificationResponseDto::from)
			.toList();
	}

	@Override
	public void sendNotification(NotificationDto dto) {

		webSocketNotificationService.send(new WebSocketNotificationDto(
			dto.userId(),
			dto.content(),
			LocalDateTime.now()
		));
	}
}
