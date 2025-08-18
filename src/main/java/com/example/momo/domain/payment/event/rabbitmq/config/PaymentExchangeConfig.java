package com.example.momo.domain.payment.event.rabbitmq.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;

/**
 * Payment 도메인이 발행하는 Exchange 정의
 */
@Configuration
public class PaymentExchangeConfig {

	/**
	 * Payment 이벤트 Exchange (Topic)
	 * - payment.completed
	 * - payment.failed
	 * - payment.refunded
	 * - payment.hold.released
	 */
	@Bean
	public TopicExchange paymentEventsExchange() {
		return ExchangeBuilder.topicExchange(RabbitExchangeNames.PAYMENT_EVENTS)
			.durable(true)
			.build();
	}

	/**
	 * Payment Dead Letter Exchange
	 *
	 * 처리 실패한 메시지들이 모이는 곳
	 * 재시도 3회 초과 시 이동
	 */
	@Bean
	public DirectExchange paymentDlxExchange() {
		return ExchangeBuilder.directExchange(RabbitExchangeNames.DLX_PAYMENT)
			.durable(true)
			.build();
	}
}