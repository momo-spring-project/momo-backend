package com.example.momo.domain.messagehub.event.rabbitmq.config;

import static com.example.momo.global.rabbitmq.constant.QueueNames.*;
import static com.example.momo.global.rabbitmq.constant.RabbitExchangeNames.*;
import static com.example.momo.global.rabbitmq.constant.RoutingKeys.*;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 메시지 허브에서 처리할 RabbitMQ 큐와 바인딩 설정.
 * 허브 메인 큐, DLQ(Dead Letter Queue), TTL 및 라우팅 키를 포함한
 * 각 도메인 이벤트(Payment, Meeting, Participant)와의 바인딩을 구성.
 */
@Configuration
public class MessageHubQueueConfig {

	//Queue

	@Bean
	public Queue hubQueue() {
		return QueueBuilder.durable(MESSAGE_HUB_QUEUE)
			.withArgument("x-dead-letter-exchange", DLX_MESSAGE_HUB)
			.withArgument("x-dead-letter-routing-key", MESSAGE_HUB_ASSEMBLE_DLQ_KEY)
			.build();
	}

	@Bean
	public Queue hubRetryQueue() {
		return QueueBuilder.durable(MESSAGE_HUB_QUEUE_RETRY)
			.withArgument("x-dead-letter-exchange", MESSAGE_HUB_EVENTS)
			.withArgument("x-dead-letter-routing-key", MESSAGE_HUB_ASSEMBLE_KEY)
			.build();
	}

	@Bean
	public Queue hubDlq() {
		return QueueBuilder.durable(MESSAGE_HUB_QUEUE_DLQ)
			.withArgument("x-message-ttl", 604800000)
			.build();
	}

	//Binding

	@Bean
	public Binding hubMainBinding() {
		return BindingBuilder.bind(hubQueue())
			.to(new TopicExchange(MESSAGE_HUB_EVENTS))
			.with(MESSAGE_HUB_ASSEMBLE_KEY); // 재유입 라우팅키
	}

	@Bean
	public Binding hubRetryBinding() {
		return BindingBuilder.bind(hubRetryQueue())
			.to(new DirectExchange(MESSAGE_HUB_EVENTS_RETRY))
			.with(MESSAGE_HUB_ASSEMBLE_RETRY_KEY);
	}

	@Bean
	public Binding hubDlqBinding(
	) {
		return BindingBuilder.bind(hubDlq())
			.to(new DirectExchange(DLX_MESSAGE_HUB))
			.with(MESSAGE_HUB_ASSEMBLE_DLQ_KEY);
	}

	@Bean
	public Binding paymentCompleteHubBinding() {
		return BindingBuilder.bind(hubQueue())
			.to(new TopicExchange(PAYMENT_EVENTS))
			.with(PAYMENT_COMPLETED_KEY);
	}

	@Bean
	public Binding paymentRefundHubBinding() {
		return BindingBuilder.bind(hubQueue())
			.to(new TopicExchange(PAYMENT_EVENTS))
			.with(PAYMENT_REFUNDED_KEY);
	}

	@Bean
	public Binding meetingCreateHubBinding() {
		return BindingBuilder.bind(hubQueue())
			.to(new TopicExchange(MEETING_EVENTS))
			.with(MEETING_CREATE_KEY);
	}

	@Bean
	public Binding meetingUpdateHubBinding() {
		return BindingBuilder.bind(hubQueue())
			.to(new TopicExchange(MEETING_EVENTS))
			.with(MEETING_UPDATE_KEY);
	}

	@Bean
	public Binding meetingDeleteHubBinding() {
		return BindingBuilder.bind(hubQueue())
			.to(new TopicExchange(MEETING_EVENTS))
			.with(MEETING_DELETE_KEY);
	}

	@Bean
	public Binding participantJoinHubBinding() {
		return BindingBuilder.bind(hubQueue())
			.to(new TopicExchange(PARTICIPANT_EVENTS))
			.with(PARTICIPANT_JOIN_KEY);
	}

	@Bean
	public Binding participantCancelHubBinding() {
		return BindingBuilder.bind(hubQueue())
			.to(new TopicExchange(PARTICIPANT_EVENTS))
			.with(PARTICIPANT_CANCEL_KEY);
	}

	// @Bean
	// public Binding followHubBinding() {
	// 	return BindingBuilder.bind(hubQueue())
	// 		.to(new TopicExchange(PARTICIPANT_EVENTS))
	// 		.with(PARTICIPANT_CANCEL_NOTIFICATION);
	// }

}
