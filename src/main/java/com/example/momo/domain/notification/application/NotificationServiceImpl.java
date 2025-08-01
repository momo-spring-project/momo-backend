package com.example.momo.domain.notification.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.notification.application.dto.NotificationDto;
import com.example.momo.domain.notification.application.dto.NotificationResponseDto;
import com.example.momo.domain.notification.domain.NotificationRepository;
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
			log.debug("알림 저장 완료: userId={}, meetingId={}, type={}, content={}",
				dto.getUserId(), dto.getTargetId(), dto.getType(), dto.getContent());
		} catch (DataIntegrityViolationException e) {
			log.warn("DB 저장 실패 - 무결성 오류: {}", e.getMessage());
		} catch (Exception e) {
			log.warn("알림 저장 실패: {}", e.getMessage(), e);
		}
	}

	@Override
	public List<NotificationResponseDto> getNotifications(Long userId) {
		List<NotificationResponseDto> notifications = notificationRepository.findAllByUserId(userId).stream()
			.map(NotificationResponseDto::from)
			.toList();

		log.debug("알림 목록 조회 완료: userId={}, count={}", userId, notifications.size());

		return notifications;
	}

	@Override
	public void sendNotification(NotificationDto dto) {

		webSocketNotificationService.send(
			WebSocketNotificationDto.builder()
				.userId(dto.getUserId())
				.meetingId(dto.getTargetId())
				.type(dto.getType())
				.content(dto.getContent())
				.createdAt(LocalDateTime.now())
				.build()
		);
	}
}
