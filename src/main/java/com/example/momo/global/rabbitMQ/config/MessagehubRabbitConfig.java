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
public class MessagehubRabbitConfig {

	public static final String HUB_QUEUE = "hub.queue";
	public static final String HUB_EXCHANGE = "hub.exchange";
	public static final String HUB_ROUTING_KEY = "hub.key";

	@Bean
	public Queue hubQueue() {
		return QueueBuilder.durable(HUB_QUEUE).build();
	}

	@Bean
	public DirectExchange hubExchange() {
		return new DirectExchange(HUB_EXCHANGE);
	}

	@Bean
	public Binding hubBinding(Queue hubQueue, DirectExchange hubExchange) {
		return BindingBuilder
			.bind(hubQueue)
			.to(hubExchange)
			.with(HUB_ROUTING_KEY);
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
