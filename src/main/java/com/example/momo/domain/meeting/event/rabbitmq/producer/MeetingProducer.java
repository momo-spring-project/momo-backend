package com.example.momo.domain.meeting.event.rabbitmq.producer;

import static com.example.momo.global.rabbitmq.constant.RabbitExchangeNames.*;
import static com.example.momo.global.rabbitmq.constant.RoutingKeys.*;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.example.momo.global.rabbitmq.dto.meeting.MeetingAlarmMessages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingProducer {

	private final RabbitTemplate rabbitTemplate;

	public void createMeetingMQ(MeetingAlarmMessages.Create event) {

		log.info("[Meeting] - MeetingProducer.createMeetingMQ : Meeting Create 메세지 발행");

		rabbitTemplate.convertAndSend(
			MESSAGE_HUB_EVENTS,
			MESSAGE_HUB_ASSEMBLE,
			event
		);
	}

	public void updateMeetingMQ(MeetingAlarmMessages.Update event) {

		log.info("[Meeting] - MeetingProducer.updateMeetingMQ : Meeting Update 메세지 발행");

		rabbitTemplate.convertAndSend(
			MESSAGE_HUB_EVENTS,
			MESSAGE_HUB_ASSEMBLE,
			event
		);
	}

	public void deleteMeetingMQ(MeetingAlarmMessages.Delete event) {

		log.info("[Meeting] - MeetingProducer.deleteMeetingMQ : Meeting Delete 메세지 발행");

		rabbitTemplate.convertAndSend(
			MESSAGE_HUB_EVENTS,
			MESSAGE_HUB_ASSEMBLE,
			event
		);
	}
}
