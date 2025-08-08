package com.example.momo.domain.meeting.event.rabbitmq.producer;

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

import static com.example.momo.global.rabbitmq.constant.RabbitExchangeNames.PARTICIPANT_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingEventPublisher {

	@Qualifier("participantRabbitTemplate")
	private final RabbitTemplate rabbitTemplate;

	/**
	 * 발행하는 이벤트 목록 ( 앞의 ParticipantEvents 생략 )
	 * : Register, Join, CancelRefund, CancelNotification
	 * RoutingKeys 상수
	 * : 카멜 케이스 -> 대문자 스네이크 케이스 (예시: PARTICIPANT_CANCEL_REFUND)
	 * <p>
	 * 일반 발행 -> 유실 가능
	 * 정상 전달 확인 발행 -> 유실되면 false 리턴, 유저 재시도
	 */

	// 일반 발행
	public void publishParticipantEvents(ParticipantEvents.ParticipantEvent event) {
		rabbitTemplate.convertAndSend(
			PARTICIPANT_EVENTS,
			event.routingKey(),
			event
		);
	}

	// 정상 전달 확인 발행
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
			PARTICIPANT_EVENTS,
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
