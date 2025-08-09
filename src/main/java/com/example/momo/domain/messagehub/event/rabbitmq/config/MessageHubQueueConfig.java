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

@Configuration
public class MessageHubQueueConfig {

	public static final int HUB_QUEUE_TTL_MS = 600_000;

	@Bean
	public Queue hubQueue() {
		return QueueBuilder.durable(MESSAGE_HUB_QUEUE)
			.withArgument("x-dead-letter-exchange", DLX_MESSAGE_HUB)
			.withArgument("x-dead-letter-routing-key", MESSAGE_HUB_ASSEMBLE_DLX)
			.withArgument("x-message-ttl", HUB_QUEUE_TTL_MS)
			.build();
	}

	@Bean
	public Queue hubDlq() {
		return QueueBuilder.durable(MESSAGE_HUB_QUEUE_DLQ)
			.withArgument("x-message-ttl", 604800000)
			.build();
	}

	@Bean
	public Binding hubDlqBinding(
	) {
		return BindingBuilder.bind(hubDlq())
			.to(new DirectExchange(DLX_MESSAGE_HUB))
			.with(MESSAGE_HUB_ASSEMBLE_DLX);
	}

	@Bean
	public Binding paymentCompleteHubBinding() {
		return BindingBuilder.bind(hubQueue())
			.to(new TopicExchange(PAYMENT_EVENTS))
			.with(PAYMENT_COMPLETED);
	}

	@Bean
	public Binding paymentRefundHubBinding() {
		return BindingBuilder.bind(hubQueue())
			.to(new TopicExchange(PAYMENT_EVENTS))
			.with(PAYMENT_REFUNDED);
	}

	@Bean
	public Binding meetingCreateHubBinding() {
		return BindingBuilder.bind(hubQueue())
			.to(new TopicExchange(MEETING_EVENTS))
			.with(MEETING_CREATE);
	}

	@Bean
	public Binding meetingUpdateHubBinding() {
		return BindingBuilder.bind(hubQueue())
			.to(new TopicExchange(MEETING_EVENTS))
			.with(MEETING_UPDATE);
	}

	@Bean
	public Binding meetingDeleteHubBinding() {
		return BindingBuilder.bind(hubQueue())
			.to(new TopicExchange(MEETING_EVENTS))
			.with(MEETING_DELETE);
	}

	@Bean
	public Binding participantJoinHubBinding() {
		return BindingBuilder.bind(hubQueue())
			.to(new TopicExchange(PARTICIPANT_EVENTS))
			.with(PARTICIPANT_JOIN);
	}

	@Bean
	public Binding participantCancelHubBinding() {
		return BindingBuilder.bind(hubQueue())
			.to(new TopicExchange(PARTICIPANT_EVENTS))
			.with(PARTICIPANT_CANCEL_NOTIFICATION);
	}

	// @Bean
	// public Binding followHubBinding() {
	// 	return BindingBuilder.bind(hubQueue())
	// 		.to(new TopicExchange(PARTICIPANT_EVENTS))
	// 		.with(PARTICIPANT_CANCEL_NOTIFICATION);
	// }

}
