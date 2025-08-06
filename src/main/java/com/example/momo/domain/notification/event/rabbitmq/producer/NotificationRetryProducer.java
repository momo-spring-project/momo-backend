package com.example.momo.domain.notification.event.rabbitmq.producer;

import static com.example.momo.global.rabbitmq.constant.RabbitExchangeNames.*;
import static com.example.momo.global.rabbitmq.constant.RoutingKeys.*;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.momo.global.rabbitmq.dto.messagehub.MessageHubNotificationMessage;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationRetryProducer {
	public static final String NOTIFICATION_RETRY_HEADER = "x-notification-retry-attempts";
	public static final int NOTIFICATION_MAX_RETRY = 3;

	private final RabbitTemplate rabbitTemplate;

	public void publishRetry(MessageHubNotificationMessage message, int nextAttempts) {
		rabbitTemplate.convertAndSend(
			NOTIFICATION_EVENTS_RETRY,
			NOTIFICATION_SENT_RETRY,
			message,
			m -> {
				m.getMessageProperties().getHeaders().put(NOTIFICATION_RETRY_HEADER, nextAttempts);
				return m;
			}
		);
	}

	public void publishToDlq(MessageHubNotificationMessage message) {
		rabbitTemplate.convertAndSend(
			NOTIFICATION_EVENTS_DLX,
			NOTIFICATION_SENT_DLX,
			message
		);
	}
}
