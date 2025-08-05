package com.example.momo.global.rabbitMQ.producer;

import static com.example.momo.global.rabbitMQ.config.MessageHubRabbitConfig.*;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.momo.global.rabbitMQ.dto.messagehub.DomainMessageEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HubMessagePublisher {
	private final RabbitTemplate rabbitTemplate;

	public void publish(DomainMessageEvent event) {
		rabbitTemplate.convertAndSend(
			HUB_EXCHANGE,
			HUB_KEY,
			event
		);
	}
}
