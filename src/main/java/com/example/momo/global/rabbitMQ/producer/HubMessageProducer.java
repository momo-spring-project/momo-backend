package com.example.momo.global.rabbitMQ.producer;

import static com.example.momo.global.rabbitMQ.constant.RabbitExchangeNames.*;
import static com.example.momo.global.rabbitMQ.constant.RoutingKeys.*;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.momo.global.rabbitMQ.dto.messagehub.DomainAlarmMessage;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HubMessageProducer {
	private final RabbitTemplate rabbitTemplate;

	public void publish(DomainAlarmMessage event) {
		rabbitTemplate.convertAndSend(
			MESSAGE_HUB_EVENTS,
			MESSAGE_HUB_ASSEMBLE,
			event
		);
	}
}
