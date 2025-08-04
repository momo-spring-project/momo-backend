package com.example.momo.domain.notification.application;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.momo.global.rabbitMQ.config.NotificationRabbitConfig;
import com.example.momo.global.rabbitMQ.dto.notification.NotificationQueueEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {
	private final NotificationHandler notificationHandler;

	@RabbitListener(
		queues = NotificationRabbitConfig.NOTIFICATION_QUEUE,
		containerFactory = "notificationListenerContainerFactory"
	)
	public void consume(NotificationQueueEvent message) {

		notificationHandler.processNotification(message);
		log.info("메세지 허브 리스너 통과 : {}", message);
	}
}
