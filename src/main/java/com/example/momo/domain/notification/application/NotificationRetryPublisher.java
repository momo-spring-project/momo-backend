package com.example.momo.domain.notification.application;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.momo.global.rabbitMQ.config.NotificationRabbitConfig;
import com.example.momo.global.rabbitMQ.dto.notification.NotificationQueueEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationRetryPublisher {
	static final String NOTIFICATION_RETRY_HEADER = "x-notification-retry-attempts";
	public static final int NOTIFICATION_MAX_RETRY = 3;

	private final RabbitTemplate rabbitTemplate;

	public void publishRetry(NotificationQueueEvent message, int nextAttempts) {
		rabbitTemplate.convertAndSend(
			NotificationRabbitConfig.NOTIFICATION_RETRY_EXCHANGE,
			NotificationRabbitConfig.NOTIFICATION_RETRY_KEY,
			message,
			m -> {
				m.getMessageProperties().getHeaders().put(NOTIFICATION_RETRY_HEADER, nextAttempts);
				return m;
			}
		);
	}

	public void publishToDlq(NotificationQueueEvent message) {
		rabbitTemplate.convertAndSend(
			NotificationRabbitConfig.NOTIFICATION_DLX,
			NotificationRabbitConfig.NOTIFICATION_DLX_KEY,
			message
		);
	}
}
