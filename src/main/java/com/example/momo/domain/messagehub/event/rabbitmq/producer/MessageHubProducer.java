package com.example.momo.domain.messagehub.event.rabbitmq.producer;

import static com.example.momo.global.rabbitmq.constant.EventTypeNames.*;
import static com.example.momo.global.rabbitmq.constant.RabbitExchangeNames.*;
import static com.example.momo.global.rabbitmq.constant.RoutingKeys.*;

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
}
