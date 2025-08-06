package com.example.momo.domain.notification.application;

import static com.example.momo.domain.notification.event.rabbitmq.producer.NotificationRetryProducer.*;

import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import com.example.momo.domain.notification.application.dto.NotificationMessageDto;
import com.example.momo.domain.notification.event.rabbitmq.producer.NotificationRetryProducer;
import com.example.momo.global.rabbitMQ.dto.messagehub.MessageHubNotificationMessage;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 알림 저장 및 전송 로직을 담당하는 컴포넌트입니다.
 * <p>
 * 도메인 이벤트 발생 이후, 알림 데이터를 DB에 저장하고 사용자에게 SSE 또는 FCM을 통해 전송합니다.
 * <p>
 * 주요 역할:
 * <ul>
 *     <li>이벤트 타입 → NotificationType 매핑</li>
 *     <li>알림 생성 및 저장</li>
 *     <li>SSE 실시간 전송 시도</li>
 *     <li>SSE 실패 시 FCM 푸시 전송</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationHandler {

	private final NotificationRetryProducer notificationRetryProducer;
	private final NotificationProvider notificationProvider;

	@Transactional
	public void handleNotification(MessageHubNotificationMessage event, Message raw) {

		NotificationMessageDto dto = NotificationMessageDto.of(event);

		if (dto == null) {
			log.warn("알림 저장 실패 - 타입 불일치: userId={}, content={}", event.getUserId(), event.getContent());
			return;
		}

		if (!tryCreateNotificationId(event, dto)) {
			handleNotificationRetry(event, raw);
			return;
		}
		if (notificationProvider.sendNotificationFromQueue(dto)) {
			return;
		}
		handleNotificationRetry(event, raw);

	}

	private boolean tryCreateNotificationId(MessageHubNotificationMessage event, NotificationMessageDto dto) {
		if (dto.getNotificationId() != null)
			return true;

		Long id = notificationProvider.saveNotificationFromQueue(dto);
		if (id == null)
			return false;

		event.updateNotificationId(id);
		dto.updateNotificationId(id);
		return true;
	}

	private void handleNotificationRetry(MessageHubNotificationMessage event, Message raw) {
		//재시도/최종 실패 분기 (헤더에서 시도 횟수 읽기)
		int attempts = ((Number)raw.getMessageProperties()
			.getHeaders().getOrDefault(NOTIFICATION_RETRY_HEADER, 0)).intValue() + 1;

		//재시도 횟수 이하거나 저장 안되어 있을 경우 재시도
		if (event.getNotificationId() == null || attempts <= NOTIFICATION_MAX_RETRY) {
			notificationRetryProducer.publishRetry(event, attempts);
			return;
		}

		notificationRetryProducer.publishToDlq(event);
	}

}