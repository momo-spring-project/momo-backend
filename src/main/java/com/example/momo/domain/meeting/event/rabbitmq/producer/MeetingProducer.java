package com.example.momo.domain.meeting.event.rabbitmq.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;
import com.example.momo.global.rabbitmq.constant.RoutingKeys;
import com.example.momo.global.rabbitmq.dto.meeting.MeetingAlarmMessages;
import com.example.momo.domain.meeting.event.springEvents.MeetingEvents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.example.momo.global.rabbitmq.constant.EventTypeNames.MEETING_DELETE;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingProducer {

	private final RabbitTemplate rabbitTemplate;

	/**
	 * 모임 생성 메세지 발행 메서드
	 */
	public void createMeetingMQ(MeetingAlarmMessages.Create event) {

		log.info("[Meeting] - MeetingProducer.createMeetingMQ : Meeting Create 메세지 발행");

		rabbitTemplate.convertAndSend(
			RabbitExchangeNames.MESSAGE_HUB_EVENTS,
			RoutingKeys.MESSAGE_HUB_ASSEMBLE_KEY,
			event
		);
	}

	/**
	 * 모임 수정 메세지 발행 메서드
	 */
	public void updateMeetingMQ(MeetingAlarmMessages.Update event) {

		log.info("[Meeting] - MeetingProducer.updateMeetingMQ : Meeting Update 메세지 발행");

		rabbitTemplate.convertAndSend(
			RabbitExchangeNames.MESSAGE_HUB_EVENTS,
			RoutingKeys.MESSAGE_HUB_ASSEMBLE_KEY,
			event
		);
	}

	/**
	 * 모임 삭제 메세지 발행 메서드
	 */
	public void deleteMeetingMQ(MeetingAlarmMessages.Delete event) {

		log.info("[Meeting] - MeetingProducer.deleteMeetingMQ : Meeting Delete 메세지 발행");

		rabbitTemplate.convertAndSend(
			RabbitExchangeNames.MESSAGE_HUB_EVENTS,
			RoutingKeys.MESSAGE_HUB_ASSEMBLE_KEY,
			event
		);
	}

	/**
	 * 모임 삭제 시 참가자들 환불 메세지 발행 메서드
	 */
	public void deleteMeetingWithRefundsMQ(MeetingEvents.Delete event) {

		log.info("[Meeting] - MeetingProducer.deleteMeetingWithRefundsMQ : Meeting Delete 시 참가자 환불 메세지 발행");

		rabbitTemplate.convertAndSend(
			RabbitExchangeNames.MEETING_EVENTS,
			MEETING_DELETE,
			event
		);
	}
}
