package com.example.momo.domain.notification.application;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.notification.application.dto.NotificationMessageDto;
import com.example.momo.domain.notification.application.dto.NotificationRequestDto;
import com.example.momo.domain.notification.application.dto.NotificationResponseDto;
import com.example.momo.domain.notification.domain.Notification;
import com.example.momo.domain.notification.domain.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

	private final NotificationRepository notificationRepository;

	//외부 알림 저장 용도
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public NotificationResponseDto createNotification(NotificationRequestDto dto) {
		try {
			Notification notification = notificationRepository.saveNotification(dto.toEntity());
			log.debug("알림 저장 완료: userId={}, meetingId={}, type={}, content={}",
				dto.getUserId(), dto.getTargetId(), dto.getType(), dto.getContent());

			return NotificationResponseDto.from(notification);
		} catch (DataIntegrityViolationException e) {
			log.warn("알림 수동 저장 실패 - 무결성 오류: {}", e.getMessage());
		} catch (Exception e) {
			log.warn("알림 수동 저장 실패: {}", e.getMessage(), e);
		}
		return null;
	}

	@Override
	public Long createNotification(NotificationMessageDto dto) {

		try {
			Notification savedNotification = notificationRepository.saveNotification(Notification.builder()
				.userId(dto.getUserId())
				.targetId(dto.getTargetId())
				.type(dto.getType())
				.content(dto.getContent())
				.build());
			log.debug("알림 저장 완료: userId={},content={}",
				savedNotification.getUserId(), savedNotification.getContent());

			return savedNotification.getId();
		} catch (DataIntegrityViolationException e) {
			log.warn("알림 저장 실패 - 무결성 오류: {}", e.getMessage());
		} catch (Exception e) {
			log.warn("알림 저장 실패: {}", e.getMessage(), e);
		}
		return null;
	}

	@Override
	public List<NotificationResponseDto> getNotifications(Long userId) {
		List<NotificationResponseDto> notifications = notificationRepository.findAllByUserId(userId).stream()
			.map(NotificationResponseDto::from)
			.toList();

		log.debug("알림 목록 조회 완료: userId={}, count={}", userId, notifications.size());

		return notifications;
	}

}
