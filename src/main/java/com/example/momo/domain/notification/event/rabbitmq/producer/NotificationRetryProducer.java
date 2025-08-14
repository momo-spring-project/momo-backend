package com.example.momo.domain.notification.event.rabbitmq.producer;

import static com.example.momo.global.rabbitmq.constant.EventTypeNames.*;
import static com.example.momo.global.rabbitmq.constant.RabbitExchangeNames.*;
import static com.example.momo.global.rabbitmq.constant.RoutingKeys.*;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.momo.global.rabbitmq.dto.common.EventWrapper;
import com.example.momo.global.rabbitmq.dto.messagehub.MessageHubNotificationMessage;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationRetryProducer {
	public static final String NOTIFICATION_RETRY_HEADER = "x-notification-retry-attempts";
	public static final int NOTIFICATION_MAX_RETRY = 3;

	private final RabbitTemplate rabbitTemplate;

	public void notificationRetry(MessageHubNotificationMessage notificationMessage, Message raw) {
		//재시도/최종 실패 분기 (헤더에서 시도 횟수 읽기)
		int attempts = ((Number)raw.getMessageProperties()
			.getHeaders().getOrDefault(NOTIFICATION_RETRY_HEADER, 0)).intValue() + 1;

		//재시도 횟수 이하거나 저장 안되어 있을 경우 재시도
		if (notificationMessage.getNotificationId() == null || attempts <= NOTIFICATION_MAX_RETRY) {
			publishRetry(EventWrapper.of(
				MESSAGE_HUB_SENT,
				notificationMessage
			), attempts);
		}
	}

	public void notificationRetry(EventWrapper<?> eventWrapper, Message raw) {
		//재시도/최종 실패 분기 (헤더에서 시도 횟수 읽기)
		int attempts = ((Number)raw.getMessageProperties()
			.getHeaders().getOrDefault(NOTIFICATION_RETRY_HEADER, 0)).intValue() + 1;

		//재시도 횟수 이하거나 저장 안되어 있을 경우 재시도
		if (attempts <= NOTIFICATION_MAX_RETRY) {
			publishRetry(eventWrapper, attempts);
		}
	}

	private void publishRetry(EventWrapper<?> eventWrapper, int nextAttempts) {
		rabbitTemplate.convertAndSend(
			NOTIFICATION_EVENTS_RETRY,
			NOTIFICATION_SENT_RETRY_KEY,
			eventWrapper,
			m -> {
				m.getMessageProperties().getHeaders().put(NOTIFICATION_RETRY_HEADER, nextAttempts);
				return m;
			}
		);
	}

}
