package com.example.momo.domain.messagehub.application.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.handler.EventRoutingHandler;
import com.example.momo.global.rabbitMQ.config.MessageHubRabbitConfig;
import com.example.momo.global.rabbitMQ.dto.messagehub.HubEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = MessageHubRabbitConfig.HUB_QUEUE)
public class MessageHubConsumer {

	private final EventRoutingHandler eventRoutingHandler;

	@RabbitListener(
		queues = MessageHubRabbitConfig.HUB_QUEUE,
		containerFactory = "hubListenerContainerFactory")
	public void consume(HubEvent event) {
		log.info("메세지 허브 리스너 접근 : {}", event);
		eventRoutingHandler.handleMessage(event);

	}

}
