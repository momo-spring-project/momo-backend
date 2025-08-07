package com.example.momo.domain.meeting.event.rabbitmq.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.example.momo.domain.meeting.event.rabbitmq.config.RabbitMQMeetingConfig;
import com.example.momo.global.springEvent.meeting.MeetingMessageEvents;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingProducer {

	private final RabbitTemplate rabbitTemplate;

	public void createMeetingMQ(MeetingMessageEvents.Create event) {

		rabbitTemplate.convertAndSend(
			RabbitMQMeetingConfig.EXCHANGE_NAME,
			RabbitMQMeetingConfig.CREATED_ROUTING_KEY,
			event
		);
	}

	public void updateMeetingMQ(MeetingMessageEvents.Update event) {

		rabbitTemplate.convertAndSend(
			RabbitMQMeetingConfig.EXCHANGE_NAME,
			RabbitMQMeetingConfig.UPDATED_ROUTING_KEY,
			event
		);
	}

	public void deleteMeetingMQ(MeetingMessageEvents.Delete event) {

		rabbitTemplate.convertAndSend(
			RabbitMQMeetingConfig.EXCHANGE_NAME,
			RabbitMQMeetingConfig.DELETED_ROUTING_KEY,
			event
		);
	}
}
