package com.example.momo.global.rabbitMQ.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationRabbitConfig {
	public static final String NOTIFICATION_QUEUE = "notification.queue";
	public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
	public static final String NOTIFICATION_ROUTING_KEY = "notification.key";

	@Bean
	public Queue notificationQueue() {
		return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
	}

	@Bean
	public DirectExchange notificationExchange() {
		return new DirectExchange(NOTIFICATION_EXCHANGE);
	}

	@Bean
	public Binding notificationBinding(Queue notificationQueue, DirectExchange notificationExchange) {
		return BindingBuilder
			.bind(notificationQueue)
			.to(notificationExchange)
			.with(NOTIFICATION_ROUTING_KEY);
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(jsonMessageConverter());
		return template;
	}
}
