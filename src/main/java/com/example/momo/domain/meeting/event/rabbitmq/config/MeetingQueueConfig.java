package com.example.momo.domain.meeting.event.rabbitmq.config;

import com.example.momo.global.rabbitmq.constant.QueueNames;
import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;
import com.example.momo.global.rabbitmq.constant.RoutingKeys;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MeetingQueueConfig {

	// Payment
	@Bean
	public Queue paymentParticipantRegisteredQueue() {
		return QueueBuilder.durable(QueueNames.PAYMENT_PARTICIPANT_REGISTER)
			.build();
	}

	@Bean
	public Queue paymentParticipantCanceledQueue() {
		return QueueBuilder.durable(QueueNames.PAYMENT_PARTICIPANT_CANCEL)
			.build();
	}

	@Bean
	public Binding paymentParticipantRegisteredBinding() {
		return BindingBuilder.bind(paymentParticipantRegisteredQueue())
			.to(new DirectExchange(RabbitExchangeNames.PARTICIPANT_EVENTS))
			.with(RoutingKeys.PARTICIPANT_REGISTER);
	}

	@Bean
	public Binding paymentParticipantCanceledBinding() {
		return BindingBuilder.bind(paymentParticipantCanceledQueue())
			.to(new DirectExchange(RabbitExchangeNames.PARTICIPANT_EVENTS))
			.with(RoutingKeys.PARTICIPANT_CANCEL);
	}


	// Notification
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
			.with(RoutingKeys.PARTICIPANT_CANCEL);
	}
}
