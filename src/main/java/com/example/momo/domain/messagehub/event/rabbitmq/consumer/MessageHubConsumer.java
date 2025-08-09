package com.example.momo.domain.messagehub.event.rabbitmq.consumer;

import static com.example.momo.global.rabbitmq.constant.QueueNames.*;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.handler.EventRoutingHandler;
import com.example.momo.global.rabbitmq.dto.common.EventWrapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
		eventRoutingHandler.handleMessage(eventWrapper.type(), eventWrapper.data());

	}

}
