package com.example.momo.domain.meeting.event.rabbitmq.producer;

import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;
import com.example.momo.global.rabbitmq.dto.ParticipantEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingEventPublisher {

	@Qualifier("participantRabbitTemplate")
	private final RabbitTemplate rabbitTemplate;

	public void publishParticipantEvents(ParticipantEvents.ParticipantEvent event) {
		rabbitTemplate.convertAndSend(
			RabbitExchangeNames.PARTICIPANT_EVENTS,
			event.routingKey(),
			event
		);
	}

	public boolean publishWithConfirmParticipantEvents(ParticipantEvents.ParticipantEvent event) {
		CompletableFuture<Boolean> future = new CompletableFuture<>();

		CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
		correlationData.getFuture().whenComplete((confirm, ex) -> {
			if (ex != null) {
				future.completeExceptionally(ex);
			} else {
				future.complete(confirm.isAck());
			}
		});

		rabbitTemplate.convertAndSend(
			RabbitExchangeNames.PARTICIPANT_EVENTS,
			event.routingKey(),
			event,
			correlationData
		);

		try {
			log.info("confirm participant event");
			return future.get(3, TimeUnit.SECONDS);
		} catch (Exception e) {
			log.error("confirm participant events time out", e);
			return false;
		}
	}
}
