package com.example.momo.domain.meeting.event.rabbitmq.producer;

import static com.example.momo.global.rabbitmq.constant.EventTypeNames.*;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;
import com.example.momo.global.rabbitmq.constant.RoutingKeys;
import com.example.momo.global.rabbitmq.dto.common.EventWrapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingProducer {

	private final RabbitTemplate rabbitTemplate;

	/**
	 * 모임 생성 메세지 발행 메서드 MeetingAlarmMessages.Create event
	 */
	public void createMeetingMQ(EventWrapper<?> event) {

		log.info("[Meeting] - MeetingProducer.createMeetingMQ : Meeting Create 메세지 발행");

		rabbitTemplate.convertAndSend(
			RabbitExchangeNames.MEETING_EVENTS,
			MEETING_CREATE,
			event
		);
	}

	/**
	 * 모임 수정 메세지 발행 메서드 MeetingAlarmMessages.Update event
	 */
	public void updateMeetingMQ(EventWrapper<?> event) {

		log.info("[Meeting] - MeetingProducer.updateMeetingMQ : Meeting Update 메세지 발행");

		rabbitTemplate.convertAndSend(
			RabbitExchangeNames.MEETING_EVENTS,
			MEETING_UPDATE,
			event
		);
	}

	/**
	 * 모임 삭제 메세지 발행 메서드 MeetingAlarmMessages.Delete event
	 */
	public void deleteMeetingMQ(EventWrapper<?> event) {

		log.info("[Meeting] - MeetingProducer.deleteMeetingMQ : Meeting Delete 메세지 발행");

		rabbitTemplate.convertAndSend(
			RabbitExchangeNames.MEETING_EVENTS,
			MEETING_DELETE,
			event
		);
	}

	/**
	 * 모임 삭제 시 참가자들 환불 메세지 발행 메서드
	 */
	public void deleteMeetingWithRefundsMQ(EventWrapper<?> wrapper) {

		log.info("[Meeting] - MeetingProducer.deleteMeetingWithRefundsMQ : Meeting Delete 시 참가자 환불 메세지 발행");

		rabbitTemplate.convertAndSend(
			RabbitExchangeNames.MEETING_EVENTS,
			RoutingKeys.MEETING_DELETE_KEY,
			wrapper
		);
	}
}
