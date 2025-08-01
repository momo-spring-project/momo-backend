package com.example.momo.domain.messagehub.application.listener;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.handler.EventRoutingHandler;
import com.example.momo.global.rabbitMQ.config.MessagehubRabbitConfig;
import com.example.momo.global.rabbitMQ.dto.messagehub.HubEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = MessagehubRabbitConfig.HUB_QUEUE)
public class MessageHubConsumer {

	private final EventRoutingHandler eventRoutingHandler;

	@RabbitHandler
	public void consume(HubEvent event) {
		eventRoutingHandler.handleMessage(event);
		log.info("메세지 허브 리스너 통과 : {}", event.toString());
	}

}
