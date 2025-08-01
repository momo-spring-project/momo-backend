package com.example.momo.domain.notification.application;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.momo.global.rabbitMQ.dto.notification.NotificationQueueEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {
	private final NotificationHandler notificationHandler;

	@RabbitListener(queues = "notification.queue")
	public void consume(NotificationQueueEvent message) {

		notificationHandler.processNotification(message);
	}
}
