package com.example.momo.domain.notification.event.rabbitmq.config;

import static com.example.momo.global.rabbitMQ.constant.QueueNames.*;
import static com.example.momo.global.rabbitMQ.constant.RabbitExchangeNames.*;
import static com.example.momo.global.rabbitMQ.constant.RoutingKeys.*;

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
public class NotificationRabbitConfig {

	public static final int NOTIFICATION_TTL_MS = 10_000;
	public static final int NOTIFICATION_RETRY_TTL_MS = 30_000;

	@Bean(name = "notificationFactory")
	public SimpleRabbitListenerContainerFactory notificationMainFactory(
		ConnectionFactory connectionFactory,
		MessageConverter messageConverter
	) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(messageConverter);
		return factory;
	}

	@Bean(name = "notificationQueue")
	public Queue notificationQueue() {
		return QueueBuilder.durable(NOTIFICATION_QUEUE)
			.withArgument("x-dead-letter-exchange", NOTIFICATION_EVENTS_DLX)
			.withArgument("x-dead-letter-routing-key", NOTIFICATION_SENT_DLX)
			.withArgument("x-message-ttl", NOTIFICATION_TTL_MS)
			.build();
	}

	@Bean(name = "notificationExchange")
	public TopicExchange notificationExchange() {
		return new TopicExchange(NOTIFICATION_EVENTS);
	}

	@Bean(name = "notificationDlxExchange")
	public TopicExchange notificationDlxExchange() {

		return new TopicExchange(NOTIFICATION_EVENTS_DLX);
	}

	@Bean(name = "notificationDlq")
	public Queue notificationDlq() {

		return QueueBuilder.durable(NOTIFICATION_QUEUE_DLQ).build();
	}

	@Bean(name = "notificationDlqBinding")
	public Binding notificationDlqBinding(
		@Qualifier("notificationDlq") Queue dlq,
		@Qualifier("notificationDlxExchange") TopicExchange dlx
	) {
		return BindingBuilder.bind(dlq).to(dlx).with(NOTIFICATION_SENT_DLX);
	}

	@Bean(name = "notificationBinding")
	public Binding notificationBinding(
		@Qualifier("notificationQueue") Queue notificationQueue,
		@Qualifier("notificationExchange") TopicExchange notificationExchange
	) {
		return BindingBuilder.bind(notificationQueue)
			.to(notificationExchange)
			.with(NOTIFICATION_SENT);
	}

	// 재시도 교환기/큐/바인딩
	@Bean
	public TopicExchange notificationRetryExchange() {
		return new TopicExchange(NOTIFICATION_EVENTS_RETRY);
	}

	@Bean
	public Queue notificationRetryQueue() {
		return QueueBuilder.durable(NOTIFICATION_QUEUE_RETRY)
			.withArgument("x-message-ttl", NOTIFICATION_RETRY_TTL_MS) // 30초 지연 후
			.withArgument("x-dead-letter-exchange", NOTIFICATION_EVENTS)  // 메인으로 복귀
			.withArgument("x-dead-letter-routing-key", NOTIFICATION_SENT) // 메인 라우팅키
			.build();
	}

	@Bean
	public Binding notificationRetryBinding() {
		return BindingBuilder.bind(notificationRetryQueue())
			.to(notificationRetryExchange())
			.with(NOTIFICATION_SENT_RETRY);
	}

}
