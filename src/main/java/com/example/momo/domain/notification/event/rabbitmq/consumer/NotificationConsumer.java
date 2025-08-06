package com.example.momo.domain.notification.event.rabbitmq.consumer;

import static com.example.momo.domain.notification.event.rabbitmq.producer.NotificationRetryProducer.*;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.momo.domain.notification.application.NotificationHandler;
import com.example.momo.global.rabbitMQ.config.NotificationRabbitConfig;
import com.example.momo.global.rabbitMQ.dto.messagehub.MessageHubNotificationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {
	private final NotificationHandler notificationHandler;

	@RabbitListener(
		queues = NotificationRabbitConfig.NOTIFICATION_QUEUE,
		containerFactory = "notificationFactory"
	)
	public void consumeMain(MessageHubNotificationEvent queueEvent, Message message) {
		int retryCount = calculateRetryCount(message);
		log.info("알림 컨슈머 접근 : userId = {}, 시도 횟수 = {}", queueEvent.getUserId(), retryCount);

		// 정상 처리 시도
		notificationHandler.handleNotification(queueEvent, message);
	}

	private static int calculateRetryCount(Message message) {
		Object object = message.getMessageProperties().getHeaders().get(NOTIFICATION_RETRY_HEADER);
		return (object instanceof Number) ? ((Number)object).intValue() : 1;
	}

}
