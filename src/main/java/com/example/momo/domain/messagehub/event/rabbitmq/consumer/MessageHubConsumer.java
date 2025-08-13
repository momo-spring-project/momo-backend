package com.example.momo.domain.messagehub.event.rabbitmq.consumer;

import static com.example.momo.global.rabbitmq.constant.QueueNames.*;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.handler.EventRoutingHandler;
import com.example.momo.global.rabbitmq.dto.common.EventWrapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 메시지 허브 큐(MESSAGE_HUB_QUEUE)에서 수신한 이벤트를 처리하는 RabbitMQ 컨슈머.
 * EventWrapper를 수신하여 타입과 데이터 유효성을 검사한 후,
 * EventRoutingHandler를 통해 해당 이벤트를 라우팅 및 처리.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHubConsumer {

	private final EventRoutingHandler eventRoutingHandler;

	@RabbitListener(
		queues = MESSAGE_HUB_QUEUE,
		containerFactory = "hubListenerContainerFactory")
	public <T> void consumeWrapper(EventWrapper<T> eventWrapper) {

		if (eventWrapper.type() == null || eventWrapper.data() == null) {
			log.error("메세지 허브 리스너 접근 실패 - null");
			return;
		}
		log.info("메세지 허브 리스너 접근 : {}", eventWrapper);
		eventRoutingHandler.handleMessage(eventWrapper.uuId(), eventWrapper.type(), eventWrapper.data());

	}

}
