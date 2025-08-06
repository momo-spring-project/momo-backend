package com.example.momo.global.rabbitMQ.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class MessageHubRabbitConfig {
	public static final String HUB_QUEUE = "hub.queue";
	public static final String HUB_EXCHANGE = "hub.exchange";
	public static final String HUB_KEY = "hub.key";

	public static final String HUB_DLX = "hub.dlx";
	public static final String HUB_DLQ = "hub.dlq";
	public static final String HUB_DLX_KEY = "hub.dlx.key";

	public static final int HUB_QUEUE_TTL_MS = 10_000;

	public static final String HUB_RETRY_EXCHANGE = "hub.retry.exchange";
	public static final String HUB_RETRY_QUEUE = "hub.retry.queue";
	public static final String HUB_RETRY_KEY = "hub.retry.key";
	public static final int HUB_RETRY_TTL_MS = 30_000; // 예시, 필요시 조정

	//리스너(Consumer) 동작 규칙 템플릿
	//메시지를 “받을 때”의 동작 규칙(컨슈머 스레드 수, ACK 모드, prefetch, 메시지 컨버터, 재시도 정책 등)을 묶어둔 설정 묶음
	@Bean(name = "hubListenerContainerFactory")
	public SimpleRabbitListenerContainerFactory hubListenerContainerFactory(
		ConnectionFactory connectionFactory,
		MessageConverter messageConverter
	) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(messageConverter);
		return factory;
	}

	@Bean(name = "hubQueue")
	public Queue hubQueue() {
		return QueueBuilder.durable(HUB_QUEUE)
			.withArgument("x-dead-letter-exchange", HUB_DLX)
			.withArgument("x-dead-letter-routing-key", HUB_DLX_KEY)
			.withArgument("x-message-ttl", HUB_QUEUE_TTL_MS)
			.build();
	}

	//DLQ (실패 메시지 수신용 큐)
	@Bean(name = "hubDlq")
	public Queue hubDlq() {

		return new Queue(HUB_DLQ, true);
	}

	@Bean(name = "hubExchange")
	public TopicExchange hubExchange() {

		return new TopicExchange(HUB_EXCHANGE);
	}

	//DLX (Dead Letter Exchange)
	@Bean(name = "hubDlxExchange")
	public TopicExchange hubDlxExchange() {

		return new TopicExchange(HUB_DLX);
	}

	//DLQ Binding
	@Bean(name = "hubDlqBinding")
	public Binding hubDlqBinding(
		@Qualifier("hubDlq") Queue dlq,
		@Qualifier("hubDlxExchange") TopicExchange dlx
	) {
		return BindingBuilder.bind(dlq).to(dlx).with(HUB_DLX_KEY);
	}

	@Bean(name = "hubBinding")
	public Binding hubBinding(
		@Qualifier("hubQueue") Queue hubQueue,
		@Qualifier("hubExchange") TopicExchange hubExchange
	) {
		return BindingBuilder.bind(hubQueue)
			.to(hubExchange)
			.with(HUB_KEY);
	}

	// Retry Exchange
	@Bean(name = "hubRetryExchange")
	public TopicExchange hubRetryExchange() {
		return new TopicExchange(HUB_RETRY_EXCHANGE);
	}

	// Retry Queue
	@Bean(name = "hubRetryQueue")
	public Queue hubRetryQueue() {
		return QueueBuilder.durable(HUB_RETRY_QUEUE)
			.withArgument("x-dead-letter-exchange", HUB_EXCHANGE)
			.withArgument("x-dead-letter-routing-key", HUB_KEY)
			.withArgument("x-message-ttl", HUB_RETRY_TTL_MS)
			.build();
	}

	// Retry Binding
	@Bean(name = "hubRetryBinding")
	public Binding hubRetryBinding(
		@Qualifier("hubRetryQueue") Queue retryQueue,
		@Qualifier("hubRetryExchange") TopicExchange retryExchange
	) {
		return BindingBuilder.bind(retryQueue)
			.to(retryExchange)
			.with(HUB_RETRY_KEY);
	}

}
