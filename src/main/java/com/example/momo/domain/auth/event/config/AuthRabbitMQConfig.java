package com.example.momo.domain.auth.event.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;
import com.example.momo.global.rabbitmq.constant.RoutingKeys;

@Configuration
public class AuthRabbitMQConfig {
	public static final String AUTH_USER_EVENTS_QUEUE = "auth.user.events.queue";
	public static final String AUTH_USER_DLQ = "auth.user.dlq";
	public static final String AUTH_USER_DLX = "auth.user.dlx";

	@Bean
	public Queue authUserEventsQueue() {
		return QueueBuilder.durable(AUTH_USER_EVENTS_QUEUE)
			.deadLetterExchange(AUTH_USER_DLX)
			.deadLetterRoutingKey(AUTH_USER_DLQ)
			.build();
	}

	@Bean
	public Queue authUserDLQ() {
		return new Queue(AUTH_USER_DLQ, true);
	}

	@Bean
	public DirectExchange authUserDLX() {
		return new DirectExchange(AUTH_USER_DLX, true, false);
	}

	@Bean
	public Binding authUserEventsBinding() {
		return BindingBuilder
			.bind(authUserEventsQueue())
			.to(new TopicExchange(RabbitExchangeNames.USER_EVENTS, true, false))
			.with(RoutingKeys.USER_WITHDRAWN_KEY);
	}

	@Bean
	public Binding authUserDLQBinding() {
		return BindingBuilder.bind(authUserDLQ()).to(authUserDLX()).with(AUTH_USER_DLQ);
	}

	@Bean("authConnectionFactory")
	public ConnectionFactory authConnectionFactory(
		@Qualifier("rabbitConnectionFactory") CachingConnectionFactory base) {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory(base.getRabbitConnectionFactory());

		connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
		connectionFactory.setPublisherReturns(true);
		return connectionFactory;
	}
}
