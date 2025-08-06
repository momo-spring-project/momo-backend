package com.example.momo.domain.user.event.rabbitmq.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;

@Configuration
public class UserRabbitMQConfig {

	@Bean
	public TopicExchange userEventsExchange() {
		return new TopicExchange(RabbitExchangeNames.USER_EVENTS, true, false);
	}
}