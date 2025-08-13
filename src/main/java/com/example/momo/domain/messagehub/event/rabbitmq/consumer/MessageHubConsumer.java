package com.example.momo.domain.messagehub.event.rabbitmq.consumer;

import static com.example.momo.domain.notification.event.rabbitmq.producer.NotificationRetryProducer.*;
import static com.example.momo.global.rabbitmq.constant.QueueNames.*;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.handler.EventRoutingHandler;
import com.example.momo.domain.messagehub.application.service.MessageHubRedisService;
import com.example.momo.domain.messagehub.enums.UuidStatus;
import com.example.momo.domain.messagehub.event.rabbitmq.producer.MessageHubProducer;
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
	private final MessageHubRedisService messageHubRedisService;
	private final MessageHubProducer producer;

	@RabbitListener(
		queues = MESSAGE_HUB_QUEUE,
		containerFactory = "hubListenerContainerFactory")
	public <T> void consumeWrapper(EventWrapper<T> eventWrapper, Message message) {

		if (eventWrapper.type() == null || eventWrapper.data() == null) {
			log.error("메세지 허브 리스너 접근 실패 - null");
			return;
		}

		UuidStatus uuidStatus = messageHubRedisService.createMessageHubUuidOrExist(eventWrapper.uuId());

		//UUID 중복 혹은 NULL
		if (uuidStatus == UuidStatus.SKIP) {
			return;
		}

		//UUID 저장 실패 - 재시도 발행
		if (uuidStatus == UuidStatus.SAVE_FAIL) {
			producer.messageHubRetry(eventWrapper, message);
			return;
		}

		int retryCount = calculateRetryCount(message);
		log.info("알림 컨슈머 접근 : Type = {} 시도 횟수 = {}", eventWrapper.type(), retryCount);
		
		eventRoutingHandler.handleMessage(eventWrapper.type(), eventWrapper.data());

	}

	private static int calculateRetryCount(Message message) {
		Object object = message.getMessageProperties().getHeaders().get(NOTIFICATION_RETRY_HEADER);
		return (object instanceof Number) ? ((Number)object).intValue() : 1;
	}

}
