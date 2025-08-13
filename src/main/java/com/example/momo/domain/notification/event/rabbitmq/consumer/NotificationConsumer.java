package com.example.momo.domain.notification.event.rabbitmq.consumer;

import static com.example.momo.domain.notification.event.rabbitmq.producer.NotificationRetryProducer.*;
import static com.example.momo.global.rabbitmq.constant.EventTypeNames.*;
import static com.example.momo.global.rabbitmq.constant.QueueNames.*;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.momo.domain.notification.application.NotificationHandler;
import com.example.momo.global.rabbitmq.dto.common.EventWrapper;
import com.example.momo.global.rabbitmq.dto.messagehub.MessageHubNotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {
	private final NotificationHandler notificationHandler;
	private final ObjectMapper objectMapper;

	@RabbitListener(
		queues = NOTIFICATION_QUEUE,
		containerFactory = "notificationFactory"
	)
	public void consumeMain(EventWrapper<?> wrapper, Message message) {

		if (!validateNotificationMessage(wrapper)) {
			return;
		}

		MessageHubNotificationMessage notificationMessage = mapping(wrapper.type(), wrapper.data());
		if (notificationMessage == null) {
			return;
		}

		int retryCount = calculateRetryCount(message);
		log.info("알림 컨슈머 접근 : userId = {}, 시도 횟수 = {}", notificationMessage.getUserId(), retryCount);

		// 정상 처리 시도
		notificationHandler.handleNotification(notificationMessage, message);
	}

	private MessageHubNotificationMessage mapping(String type, Object object) {

		try {
			MessageHubNotificationMessage notificationMessage = objectMapper.convertValue(object,
				MessageHubNotificationMessage.class);

			if (notificationMessage == null || notificationMessage.getUserId() == null) {
				log.error("알림 컨슈머 접근 실패 - 데이터 유실 dto ={}", notificationMessage);
				return null;
			}

			return notificationMessage;
		} catch (IllegalArgumentException exception) {
			log.error("알림 컨슈머 접근 실패 - 형변환 실패 타입 불일치 type={} object={}", type, object);
			return null;
		}
	}

	private static int calculateRetryCount(Message message) {
		Object object = message.getMessageProperties().getHeaders().get(NOTIFICATION_RETRY_HEADER);
		return (object instanceof Number) ? ((Number)object).intValue() : 1;
	}

	private boolean validateNotificationMessage(EventWrapper<?> wrapper) {
		if (!MESSAGE_HUB_SENT.equals(wrapper.type())) {
			log.error("알림 컨슈머 접근 실패 - 타입 불일치 type={}", wrapper.type());
			return false;
		}

		return true;

	}

}
