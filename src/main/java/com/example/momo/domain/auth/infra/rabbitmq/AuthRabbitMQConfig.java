package com.example.momo.domain.auth.infra.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthRabbitMQConfig {

	public static final String AUTH_USER_EVENTS_QUEUE = "auth.user.events.queue";

	@Bean
	public Queue authUserEventsQueue() {
		return new Queue(AUTH_USER_EVENTS_QUEUE, true);
	}

	@Bean
	public Binding authUserEventsBinding() {
		return BindingBuilder
			.bind(authUserEventsQueue())
			.to(new TopicExchange("momo.user.events", true, false))
			.with("user.withdrawn");
	}
}
