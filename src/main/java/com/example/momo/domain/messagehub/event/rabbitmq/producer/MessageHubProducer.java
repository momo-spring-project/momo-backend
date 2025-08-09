package com.example.momo.domain.messagehub.event.rabbitmq.producer;

import static com.example.momo.global.rabbitmq.constant.EventTypeNames.*;
import static com.example.momo.global.rabbitmq.constant.RabbitExchangeNames.*;
import static com.example.momo.global.rabbitmq.constant.RoutingKeys.*;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.momo.global.rabbitmq.dto.common.EventWrapper;
import com.example.momo.global.rabbitmq.dto.messagehub.MessageHubNotificationMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHubProducer {
	private final RabbitTemplate rabbitTemplate;

	public void publish(MessageHubNotificationMessage message) {
		rabbitTemplate.convertAndSend(
			MESSAGE_HUB_EVENTS,
			MESSAGE_HUB_ASSEMBLE,
			EventWrapper.of(
				MESSAGE_HUB_SENT,
				message
			)
		);
	}
}
