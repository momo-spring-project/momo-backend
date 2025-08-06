package com.example.momo.domain.meeting.event.rabbitmq.producer;

import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;
import com.example.momo.global.rabbitmq.dto.ParticipantEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MeetingEventPublisher {

	private final RabbitTemplate rabbitTemplate;

	public void publishParticipantEvents(ParticipantEvents.ParticipantEvent event) {
		rabbitTemplate.convertAndSend(
			RabbitExchangeNames.PARTICIPANT_EVENTS,
			event.routingKey(),
			event
		);
	}
}
