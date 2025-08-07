package com.example.momo.domain.meeting.event.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.momo.global.rabbitmq.constant.QueueNames;
import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;
import com.example.momo.global.rabbitmq.constant.RoutingKeys;

@Configuration
public class MeetingQueueConfig {

	// Notification 배달 예정
	@Bean
	public Queue notificationParticipantJoinedQueue() {
		return QueueBuilder.durable(QueueNames.NOTIFICATION_PARTICIPANT_JOIN)
			.build();
	}

	@Bean
	public Queue notificationParticipantCanceledQueue() {
		return QueueBuilder.durable(QueueNames.NOTIFICATION_PARTICIPANT_CANCEL)
			.build();
	}

	@Bean
	public Binding notificationParticipantJoinedBinding() {
		return BindingBuilder.bind(notificationParticipantJoinedQueue())
			.to(new DirectExchange(RabbitExchangeNames.PARTICIPANT_EVENTS))
			.with(RoutingKeys.PARTICIPANT_JOIN);
	}

	@Bean
	public Binding notificationParticipantCanceledBinding() {
		return BindingBuilder.bind(notificationParticipantCanceledQueue())
			.to(new DirectExchange(RabbitExchangeNames.PARTICIPANT_EVENTS))
			.with(RoutingKeys.PARTICIPANT_CANCEL_NOTIFICATION);
	}

	@Bean
	public Queue paymentParticipantCreatedQueue() {
		return QueueBuilder.durable("payment.participant.created.queue").build();
	}

	@Bean
	public Binding paymentParticipantCreatedBinding() {
		return BindingBuilder.bind(paymentParticipantCreatedQueue())
			.to(new TopicExchange(RabbitExchangeNames.PAYMENT_EVENTS))
			.with(RoutingKeys.PAYMENT_COMPLETED);
	}
}
