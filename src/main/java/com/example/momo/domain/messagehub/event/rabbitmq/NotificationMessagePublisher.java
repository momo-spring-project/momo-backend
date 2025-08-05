package com.example.momo.domain.messagehub.event.rabbitmq;

import static com.example.momo.global.rabbitMQ.config.NotificationRabbitConfig.*;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.momo.global.rabbitMQ.dto.messagehub.MessageHubNotificationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessagePublisher {
	private final RabbitTemplate rabbitTemplate;

	public void publish(MessageHubNotificationEvent event) {
		rabbitTemplate.convertAndSend(
			NOTIFICATION_EXCHANGE,
			NOTIFICATION_KEY,
			event
		);
	}
}
