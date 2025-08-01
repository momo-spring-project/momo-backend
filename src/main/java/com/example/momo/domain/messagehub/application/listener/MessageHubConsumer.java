package com.example.momo.domain.messagehub.application.listener;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.handler.EventRoutingHandler;
import com.example.momo.global.rabbitMQ.dto.messagehub.HubEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = "hub.queue")
public class MessageHubConsumer {

	private final EventRoutingHandler eventRoutingHandler;

	@RabbitHandler
	public void consume(HubEvent event) {
		eventRoutingHandler.handleMessage(event); // 분기는 handler 한 곳에서만
	}

}
