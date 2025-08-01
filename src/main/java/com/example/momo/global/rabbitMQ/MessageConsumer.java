package com.example.momo.global.rabbitMQ;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MessageConsumer {

	//@RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
	public void receive(String message) {
		try {
			log.info("Received message: {}", message);
			// 메시지 처리 로직
		} catch (Exception e) {
			log.error("Error processing message: {}", message, e);
			// 필요시 DLQ(Dead Letter Queue)로 전송
			throw new AmqpRejectAndDontRequeueException("Failed to process message", e);
		}
	}
}