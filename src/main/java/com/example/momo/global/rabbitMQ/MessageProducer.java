package com.example.momo.global.rabbitMQ;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.example.momo.global.config.RabbitMQConfig;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MessageProducer {

	private final RabbitTemplate rabbitTemplate;

	//메세지를 exchange에 publish
	public void send(String message) {
		rabbitTemplate.convertAndSend(
			RabbitMQConfig.EXCHANGE_NAME,
			RabbitMQConfig.ROUTING_KEY,
			message
		);
	}
}