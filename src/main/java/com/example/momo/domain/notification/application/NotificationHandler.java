package com.example.momo.domain.notification.application;

import org.springframework.stereotype.Service;

import com.example.momo.domain.notification.application.dto.NotificationRequestDto;
import com.example.momo.domain.notification.application.fcm.FcmService;
import com.example.momo.domain.notification.application.sse.SseService;
import com.example.momo.domain.notification.application.sse.dto.SseMessageDto;
import com.example.momo.domain.notification.domain.Notification;
import com.example.momo.domain.notification.enums.NotificationType;
import com.example.momo.global.infrastructure.springEvent.notification.MessageEvents;

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
	private final SseService sseService;
	private final FcmService fcmService;

	/**
	 * 모임 알림 이벤트를 처리합니다.
	 * DB에 알림 정보를 저장하고, 사용자에게 실시간으로 알림을 전송합니다.
	 * 이 메서드는 하나의 트랜잭션 내에서 동작하며, 저장 및 전송이 함께 처리됩니다.
	 *
	 * @param event 처리할 알림 이벤트 정보
	 */
	@Transactional
	public void processNotification(MessageEvents event) {
		NotificationType type;
		if ((type = resolveNotificationType(event.typeName())) == null) {
			return;
		}

		Long targetId = event.targetId();
		String content = event.content();

		for (Long userId : event.userIdList()) {
			//알림 이벤트 DB 저장
			Notification notification = notificationService.createNotification(NotificationRequestDto.builder()
				.userId(userId)
				.targetId(targetId)
				.type(type)
				.content(content)
				.build());

			if (notification == null) {
				log.warn("알림 저장 실패: userId={}, content={}", userId, content);
				//todo : 저장 실패 알림 메세지큐 저장
				continue;
			}

			//SSE 전송 시도
			boolean sseSuccess = sseService.sendIfConnected(SseMessageDto.from(notification));

			//SSE 전송 실패 시 FCM 전송 시도
			if (!sseSuccess) {
				fcmService.processFcmIfTokenExists(notification);
			}
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