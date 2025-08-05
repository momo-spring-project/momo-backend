package com.example.momo.domain.meeting.event.rabbitmq.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.event.rabbitmq.config.RabbitMQElasticsearchConfig;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MeetingElasticsearchProducer {

	private final RabbitTemplate rabbitTemplate;

	public void saveElasticsearch(Meeting meeting) {

		rabbitTemplate.convertAndSend(
			RabbitMQElasticsearchConfig.EXCHANGE_NAME,
			RabbitMQElasticsearchConfig.SAVED_ROUTING_KEY,
			meeting
		);
	}

	public void deleteElasticsearch(Meeting meeting) {

		rabbitTemplate.convertAndSend(
			RabbitMQElasticsearchConfig.EXCHANGE_NAME,
			RabbitMQElasticsearchConfig.DELETED_ROUTING_KEY,
			meeting
		);
	}
}
