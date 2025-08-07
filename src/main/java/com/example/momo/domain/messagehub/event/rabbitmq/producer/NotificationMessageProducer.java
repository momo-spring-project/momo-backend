package com.example.momo.domain.messagehub.event.rabbitmq.producer;

import static com.example.momo.global.rabbitmq.constant.RabbitExchangeNames.*;
import static com.example.momo.global.rabbitmq.constant.RoutingKeys.*;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.momo.global.rabbitmq.dto.messagehub.MessageHubNotificationMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageProducer {
	private final RabbitTemplate rabbitTemplate;

	public void publish(MessageHubNotificationMessage event) {
		rabbitTemplate.convertAndSend(
			NOTIFICATION_EVENTS,
			NOTIFICATION_SENT,
			event
		);
	}
}
