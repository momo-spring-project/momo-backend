package com.example.momo.domain.meeting.event.rabbitmq.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.example.momo.global.rabbitmq.constant.RabbitExchangeNames.*;

@Configuration
public class MeetingExchangeConfig {

	@Bean
	public TopicExchange meetingEventsExchange() {
		return ExchangeBuilder.topicExchange(MEETING_EVENTS)
			.durable(true)
			.build();
	}

	@Bean
	public TopicExchange participantEventsExchange() {
		return ExchangeBuilder.topicExchange(PARTICIPANT_EVENTS)
			.durable(true)
			.build();
	}

	@Bean
	public DirectExchange participantDlxExchange() {
		return ExchangeBuilder.directExchange(DLX_PARTICIPANT)
			.durable(true)
			.build();
	}
}
