package com.example.momo.domain.messagehub.event.rabbitmq.config;

import static com.example.momo.global.rabbitmq.constant.RabbitExchangeNames.*;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 메시지 허브에서 사용하는 RabbitMQ 교환기 설정.
 * 이벤트 전달용 TopicExchange와 DLX(Dead Letter Exchange)용 DirectExchange를 생성.
 */
@Configuration
public class MessageHubExchangeConfig {

	@Bean
	public TopicExchange hubExchange() {

		return ExchangeBuilder.topicExchange(MESSAGE_HUB_EVENTS)
			.durable(true)
			.build();
	}

	@Bean
	public DirectExchange hubRetryExchange() {

		return ExchangeBuilder.directExchange(MESSAGE_HUB_EVENTS_RETRY)
			.durable(true)
			.build();
	}

	@Bean
	public DirectExchange hubDlxExchange() {

		return ExchangeBuilder.directExchange(DLX_MESSAGE_HUB)
			.durable(true)
			.build();
	}
}
