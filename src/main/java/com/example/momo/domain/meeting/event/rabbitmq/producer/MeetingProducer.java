package com.example.momo.domain.meeting.event.rabbitmq.producer;

import static com.example.momo.global.rabbitmq.constant.RabbitExchangeNames.*;
import static com.example.momo.global.rabbitmq.constant.RoutingKeys.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;
import com.example.momo.global.rabbitmq.dto.common.EventWrapper;
import com.example.momo.global.rabbitmq.dto.meeting.MeetingEvents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingProducer {

	private final RabbitTemplate rabbitTemplate;

	@Qualifier("participantRabbitTemplate")
	private final RabbitTemplate meetingRabbitTemplate;

	/**
	 * 모임 생성 메세지 발행 메서드 MeetingAlarmMessages.Create event
	 */
	public void createMeetingMQ(EventWrapper<?> event) {

		log.info("[Meeting] - MeetingProducer.createMeetingMQ : Meeting Create 메세지 발행");

		rabbitTemplate.convertAndSend(
			RabbitExchangeNames.MEETING_EVENTS,
			MEETING_CREATE_KEY,
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
			MEETING_UPDATE_KEY,
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
			MEETING_DELETE_KEY,
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
			MEETING_DELETE_KEY,
			wrapper
		);
	}

	// 일반 발행
	public void publishParticipantEvents(MeetingEvents.MeetingEvent event, String eventType,
		String routingKey) {
		try {
			EventWrapper<?> wrapper = EventWrapper.of(eventType, event);

			meetingRabbitTemplate.convertAndSend(
				PARTICIPANT_EVENTS,
				routingKey,
				wrapper
			);

			log.info("[참가자 이벤트 발행] 발행 성공 : event = {}", event);
		} catch (Exception e) {
			log.error("[참가자 이벤트 발행] 발행 실패 : event = {}", event, e);
			throw new RuntimeException(e);
		}
	}

	// 메세지 발행
	@Transactional
	public void publishWithConfirmParticipantEvents(EventWrapper<?> wrapper, String routingKey) {
		CompletableFuture<Boolean> future = new CompletableFuture<>();

		CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
		correlationData.getFuture().whenComplete((confirm, ex) -> {
			if (ex != null) {
				future.completeExceptionally(ex);
			} else {
				future.complete(confirm.isAck());
			}
		});

		try {
			meetingRabbitTemplate.convertAndSend(
				PARTICIPANT_EVENTS,
				routingKey,
				wrapper,
				correlationData
			);
			future.get(2, TimeUnit.SECONDS);
			log.info("[참가자 이벤트 발행] 발행 성공 : event = {}", wrapper);
		} catch (Exception e) {
			log.error("[참가자 이벤트 발행] 발행 실패 : event = {}", wrapper, e);
			throw new RuntimeException(e);
		}
	}
}
