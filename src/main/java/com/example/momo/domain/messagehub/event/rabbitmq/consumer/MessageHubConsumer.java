package com.example.momo.domain.messagehub.event.rabbitmq.consumer;

import static com.example.momo.global.rabbitMQ.constant.QueueNames.*;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.handler.EventRoutingHandler;
import com.example.momo.global.rabbitMQ.dto.messagehub.DomainAlarmMessage;

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
	public void consume(DomainAlarmMessage event) {
		log.info("메세지 허브 리스너 접근 : {}", event);
		eventRoutingHandler.handleMessage(event);

	}

}
