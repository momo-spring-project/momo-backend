package com.example.momo.domain.notification.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.momo.domain.notification.domain.dto.NotificationDto;
import com.example.momo.domain.notification.enums.NotificationType;
import com.example.momo.global.infrastructure.springEvent.NotificationEvent;
import com.example.momo.global.socket.dto.WebSocketNotificationDto;
import com.example.momo.global.socket.service.WebSocketNotificationService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 알림 처리 흐름을 담당하는 컴포넌트입니다.
 * 이 클래스는 도메인 이벤트 발생 이후의 후속 처리를 담당하며,
 * 비즈니스 로직과 전송 로직 사이의 흐름을 제어하는 역할을 합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationHandler {

	private final NotificationService notificationService;
	private final WebSocketNotificationService webSocketNotificationService;

	/**
	 * 모임 알림 이벤트를 처리합니다.
	 * DB에 알림 정보를 저장하고, 사용자에게 실시간으로 알림을 전송합니다.
	 * 이 메서드는 하나의 트랜잭션 내에서 동작하며, 저장 및 전송이 함께 처리됩니다.
	 *
	 * @param event 처리할 알림 이벤트 정보
	 */
	@Transactional
	public void processMeeting(NotificationEvent event) {
		NotificationType type;
		if ((type = resolveNotificationType(event.typeName())) == null) {
			return;
		}

		Long meetingId = event.meetingId();
		String content = event.content();

		for (Long userId : event.userIdList()) {
			//DB 저장
			notificationService.createNotification(NotificationDto.builder()
				.userId(userId)
				.meetingId(meetingId)
				.type(type)
				.content(content)
				.build());

			//사용자에게 전송
			webSocketNotificationService.send(
				WebSocketNotificationDto.builder()
					.userId(userId)
					.meetingId(meetingId)
					.type(type)
					.content(content)
					.createdAt(LocalDateTime.now())
					.build()
			);
		}

	}

	private NotificationType resolveNotificationType(String typeName) {
		try {
			return NotificationType.valueOf(typeName);
		} catch (IllegalArgumentException | NullPointerException e) {
			log.warn("잘못된 NotificationType 수신: {}", typeName, e);
			return null;
		}
	}
}