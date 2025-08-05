package com.example.momo.domain.payment.infra.rabbitmq.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Payment 도메인이 발행하는 Exchange 정의
 */
@Configuration
public class PaymentExchangeConfig {

	// Exchange 이름
	public static final String X_PAYMENT_EVENTS = "x.payment.events";
	public static final String X_PAYMENT_DELAY = "x.payment.delay";
	public static final String X_DLX_PAYMENT = "dlx.payment";

	/**
	 * Payment 이벤트 Exchange (Topic)
	 * - payment.completed
	 * - payment.failed
	 * - payment.refunded
	 * - payment.hold.released
	 */
	@Bean
	public TopicExchange paymentEventsExchange() {
		return ExchangeBuilder.topicExchange(X_PAYMENT_EVENTS)
			.durable(true)
			.build();
	}

	/**
	 * 지연 처리용 Exchange
	 */
	@Bean
	public DirectExchange paymentDelayExchange() {
		return ExchangeBuilder.directExchange(X_PAYMENT_DELAY)
			.durable(true)
			.build();
	}

	/**
	 * Dead Letter Exchange
	 */
	@Bean
	public DirectExchange paymentDlxExchange() {
		return ExchangeBuilder.directExchange(X_DLX_PAYMENT)
			.durable(true)
			.build();
	}
}