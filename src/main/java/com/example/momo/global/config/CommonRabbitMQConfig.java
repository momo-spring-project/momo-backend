package com.example.momo.global.config;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import lombok.extern.slf4j.Slf4j;

/**
 * RabbitMQ 공통 설정
 * - 모든 도메인에서 공통으로 사용하는 기본 설정
 * - 각 도메인은 필요시 커스터마이징 가능
 */
@Slf4j
@Configuration
@EnableRabbit
public class CommonRabbitMQConfig {

	/**
	 * JSON 메시지 컨버터
	 */
	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	/**
	 * 기본 ConnectionFactory
	 */
	@Primary
	@Bean
	@ConfigurationProperties(prefix = "spring.rabbitmq")
	public CachingConnectionFactory rabbitConnectionFactory() {
		return new CachingConnectionFactory();
	}

	/**
	 * 기본 RabbitTemplate
	 */
	@Primary
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
		MessageConverter messageConverter) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(messageConverter);
		return template;
	}

}