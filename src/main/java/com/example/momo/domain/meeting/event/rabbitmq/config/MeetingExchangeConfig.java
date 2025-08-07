package com.example.momo.domain.meeting.event.rabbitmq.config;

import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MeetingExchangeConfig {

	@Bean
	public TopicExchange meetingEventsExchange() {
		return ExchangeBuilder.topicExchange(RabbitExchangeNames.MEETING_EVENTS)
			.durable(true)
			.build();
	}

	@Bean
	public TopicExchange participantEventsExchange() {
		return ExchangeBuilder.topicExchange(RabbitExchangeNames.PARTICIPANT_EVENTS)
			.durable(true)
			.build();
	}

	@Bean
	public DirectExchange participantDlxExchange() {
		return ExchangeBuilder.directExchange(RabbitExchangeNames.DLX_PARTICIPANT)
			.durable(true)
			.build();
	}
}
