package com.example.momo.domain.user.event.rabbitmq.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserRabbitMQConfig {

	public static final String USER_EVENTS_EXCHANGE = "momo.user.events";

	@Bean
	public TopicExchange userEventsExchange() {
		return new TopicExchange(USER_EVENTS_EXCHANGE, true, false);
	}
}