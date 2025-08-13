package com.example.momo.domain.meeting.event.rabbitmq.producer;

import com.example.momo.domain.meeting.application.MeetingPaymentOutboxService;
import com.example.momo.domain.meeting.domain.MeetingPaymentOutbox;
import com.example.momo.global.rabbitmq.dto.common.EventWrapper;
import com.example.momo.global.rabbitmq.dto.meeting.ParticipantEvents;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.example.momo.global.rabbitmq.constant.RabbitExchangeNames.PARTICIPANT_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingEventPublisher {

	@Qualifier("participantRabbitTemplate")
	private final RabbitTemplate rabbitTemplate;
	private final ObjectMapper objectMapper;
	private final MeetingPaymentOutboxService meetingPaymentOutboxService;

	/**
	 * 발행하는 이벤트 목록 ( EventWrapper<?> 타입으로 발행 )
	 * : Register, Join, Cancel
	 * RoutingKeys 상수
	 * : 글로벌 RoutingKeys 참고 ( 예시 : PARTICIPANT_JOIN_KEY )
	 * <p>
	 * 일반 발행 -> 유실 가능
	 * 정상 전달 확인 발행 -> 유실되면 false 리턴, 유저 재시도
	 */

	// 일반 발행
	@Transactional
	public void publishParticipantEvents(ParticipantEvents.ParticipantEvent event, String eventType,
		String routingKey) {
		try {
			EventWrapper<?> wrapper = EventWrapper.of(eventType, event);

			rabbitTemplate.convertAndSend(
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
	public void publishWithConfirmParticipantEvents(ParticipantEvents.ParticipantEvent event, String eventType,
		String routingKey) {
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
			EventWrapper<?> wrapper = EventWrapper.of(eventType, event);

			MeetingPaymentOutbox outbox = MeetingPaymentOutbox.create(
				eventType,
				event.meetingId(),
				wrapper.uuId(),
				objectMapper.writeValueAsString(event)
			);

			meetingPaymentOutboxService.savePaymentOutbox(outbox);

			rabbitTemplate.convertAndSend(
				PARTICIPANT_EVENTS,
				routingKey,
				wrapper,
				correlationData
			);
			future.get(2, TimeUnit.SECONDS);
			meetingPaymentOutboxService.markEventAsPublished2(outbox.getEventUuid());
			log.info("[참가자 이벤트 발행] 발행 성공 : event = {}", event);
		} catch (Exception e) {
			log.error("[참가자 이벤트 발행] 발행 실패 : event = {}", event, e);
			throw new RuntimeException(e);
		}
	}
}
