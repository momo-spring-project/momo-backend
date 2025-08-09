package com.example.momo.domain.notification.event.rabbitmq.config;

import static com.example.momo.global.rabbitmq.constant.QueueNames.*;
import static com.example.momo.global.rabbitmq.constant.RabbitExchangeNames.*;
import static com.example.momo.global.rabbitmq.constant.RoutingKeys.*;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class NotificationRabbitConfig {

	public static final int NOTIFICATION_TTL_MS = 600_000;
	public static final int NOTIFICATION_RETRY_TTL_MS = 0;

	@Bean(name = "notificationFactory")
	public SimpleRabbitListenerContainerFactory notificationMainFactory(
		ConnectionFactory connectionFactory,
		MessageConverter messageConverter
	) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(messageConverter);
		factory.setConcurrentConsumers(3);
		factory.setMaxConcurrentConsumers(10);
		factory.setPrefetchCount(50);

		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		factory.setDefaultRequeueRejected(false);
		RetryOperationsInterceptor retry = RetryInterceptorBuilder.stateless()
			.maxAttempts(3)
			.backOffOptions(200, 2.0, 5000)
			.recoverer(new RejectAndDontRequeueRecoverer())
			.build();
		factory.setAdviceChain(retry);
		return factory;
	}

	@Bean
	public Queue notificationQueue() {
		return QueueBuilder.durable(NOTIFICATION_QUEUE)
			.withArgument("x-dead-letter-exchange", DLX_NOTIFICATION)
			.withArgument("x-dead-letter-routing-key", NOTIFICATION_SENT_DLX)
			.withArgument("x-message-ttl", NOTIFICATION_TTL_MS)
			.build();
	}

	@Bean
	public TopicExchange notificationExchange() {

		return new TopicExchange(NOTIFICATION_EVENTS);
	}

	@Bean
	public Binding notificationBinding(
	) {
		return BindingBuilder.bind(notificationQueue())
			.to(new TopicExchange(MESSAGE_HUB_EVENTS))
			.with(MESSAGE_HUB_ASSEMBLE);
	}

	@Bean
	public TopicExchange notificationRetryExchange() {
		return new TopicExchange(NOTIFICATION_EVENTS_RETRY);
	}

	@Bean
	public Queue notificationRetryQueue() {
		return QueueBuilder.durable(NOTIFICATION_QUEUE_RETRY)
			.withArgument("x-message-ttl", NOTIFICATION_RETRY_TTL_MS)
			.withArgument("x-dead-letter-exchange", NOTIFICATION_EVENTS)
			.withArgument("x-dead-letter-routing-key", NOTIFICATION_SENT)
			.build();
	}

	@Bean
	public Binding notificationRetryBinding() {
		return BindingBuilder.bind(notificationRetryQueue())
			.to(notificationRetryExchange())
			.with(NOTIFICATION_SENT_RETRY);
	}

}
