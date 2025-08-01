package com.example.momo.global.rabbitMQ.producer;

import static com.example.momo.global.rabbitMQ.config.NotificationRabbitConfig.*;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.momo.global.rabbitMQ.dto.notification.NotificationQueueEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessagePublisher {
	private final RabbitTemplate rabbitTemplate;

	public void publish(NotificationQueueEvent message) {
		rabbitTemplate.convertAndSend(
			NOTIFICATION_EXCHANGE,
			NOTIFICATION_ROUTING_KEY,
			message
		);
	}
}
