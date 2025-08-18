package com.example.momo.domain.messagehub.event.rabbitmq.producer;

import static com.example.momo.global.rabbitmq.constant.EventTypeNames.*;
import static com.example.momo.global.rabbitmq.constant.RabbitExchangeNames.*;
import static com.example.momo.global.rabbitmq.constant.RoutingKeys.*;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.momo.global.rabbitmq.dto.common.EventWrapper;
import com.example.momo.global.rabbitmq.dto.messagehub.MessageHubNotificationMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 메시지 허브 알림 이벤트를 RabbitMQ로 발행하는 프로듀서.
 * 지정된 교환기와 라우팅 키를 사용해 EventWrapper 형태로 메시지를 전송.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHubProducer {
	private final RabbitTemplate rabbitTemplate;

	public static final String MESSAGE_HUB_RETRY_HEADER = "x-message-hub-retry-attempts";
	public static final int MESSAGE_HUB_MAX_RETRY = 3;

	public void publish(MessageHubNotificationMessage message) {
		rabbitTemplate.convertAndSend(
			MESSAGE_HUB_EVENTS,
			MESSAGE_HUB_ASSEMBLE_KEY,
			EventWrapper.of(
				MESSAGE_HUB_SENT,
				message
			)
		);
	}

	public void messageHubRetry(EventWrapper<?> event, Message raw) {
		//재시도/최종 실패 분기 (헤더에서 시도 횟수 읽기)
		int attempts = ((Number)raw.getMessageProperties()
			.getHeaders().getOrDefault(MESSAGE_HUB_RETRY_HEADER, 0)).intValue() + 1;

		//재시도 횟수 이하면 재시도
		if (attempts <= MESSAGE_HUB_MAX_RETRY) {
			publishRetry(event, attempts);
		}
		log.error("메세지 저장 실패 - event : {}", event);
	}

	public void publishRetry(EventWrapper<?> event, int nextAttempts) {
		rabbitTemplate.convertAndSend(
			MESSAGE_HUB_EVENTS_RETRY,
			MESSAGE_HUB_ASSEMBLE_RETRY_KEY,
			event,
			m -> {
				m.getMessageProperties().getHeaders().put(MESSAGE_HUB_RETRY_HEADER, nextAttempts);
				return m;
			}
		);
	}
}
