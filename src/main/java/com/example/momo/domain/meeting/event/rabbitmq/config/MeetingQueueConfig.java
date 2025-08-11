package com.example.momo.domain.meeting.event.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.example.momo.global.rabbitmq.constant.QueueNames.*;
import static com.example.momo.global.rabbitmq.constant.RabbitExchangeNames.DLX_PARTICIPANT;
import static com.example.momo.global.rabbitmq.constant.RabbitExchangeNames.PAYMENT_EVENTS;
import static com.example.momo.global.rabbitmq.constant.RoutingKeys.*;

@Configuration
public class MeetingQueueConfig {

	// 참가자 Queue
	@Bean
	public Queue participantPaymentSuccessQueue() {
		return QueueBuilder.durable(PARTICIPANT_PAYMENT_SUCCEED)
			.withArgument("x-dead-letter-exchange", DLX_PARTICIPANT)
			.withArgument("x-dead-letter-routing-key", DLQ_PARTICIPANT)
			.build();
	}

	@Bean
	public Queue participantPaymentFailQueue() {
		return QueueBuilder.durable(PARTICIPANT_PAYMENT_FAILED)
			.withArgument("x-dead-letter-exchange", DLX_PARTICIPANT)
			.withArgument("x-dead-letter-routing-key", DLQ_PARTICIPANT)
			.build();
	}

	// 참가자 DLQ
	@Bean
	public Queue participantDlq() {
		return QueueBuilder.durable(DLQ_PARTICIPANT)
			.withArgument("x-message-ttl", 604800000)  // 7일 후 자동 삭제
			.build();
	}

	// 참가자 Binding
	@Bean
	public Binding participantPaymentSuccessBinding() {
		return BindingBuilder.bind(participantPaymentSuccessQueue())
			.to(new TopicExchange(PAYMENT_EVENTS))
			.with(PAYMENT_COMPLETED_KEY);
	}

	@Bean
	public Binding participantPaymentFailBinding() {
		return BindingBuilder.bind(participantPaymentFailQueue())
			.to(new TopicExchange(PAYMENT_EVENTS))
			.with(PAYMENT_FAILED_KEY);
	}

	// 참가자 DLQ 바인딩
	@Bean
	public Binding participantDlqBinding()  {
		return BindingBuilder.bind(participantDlq())
			.to(new DirectExchange(DLX_PARTICIPANT))
			.with(PARTICIPANT_DLQ_KEY);
	}
}
