package com.example.momo.domain.meeting.event.rabbitmq.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQMeetingConfig {

	public static final String EXCHANGE_NAME = "meeting.exchange";

	public static final String CREATED_ROUTING_KEY = "meeting.created";
	public static final String UPDATED_ROUTING_KEY = "meeting.updated";
	public static final String DELETED_ROUTING_KEY = "meeting.deleted";
	public static final String STATUS_ROUTING_KEY = "meeting.status";

	@Bean
	public TopicExchange meetingExchange() {
		return new TopicExchange(EXCHANGE_NAME);
	}

}
