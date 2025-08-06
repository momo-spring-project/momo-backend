package com.example.momo.domain.user.event.rabbitmq.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class UserRabbitMQConfig {

	// ===========================
	// Connection Factory
	// ===========================

	/**
	 * User 전용 ConnectionFactory
	 * - Publisher Confirm 및 Publisher Returns 활성화
	 */
	@Bean("userConnectionFactory")
	public ConnectionFactory userConnectionFactory(
		@Qualifier("rabbitConnectionFactory") CachingConnectionFactory cachingConnectionFactory) {
		CachingConnectionFactory factory = new CachingConnectionFactory(
			cachingConnectionFactory.getRabbitConnectionFactory());

		factory.setHost("localhost");

		// Publisher Confirm 설정 - 메시지 발행 성공/실패 확인
		factory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);

		// Publisher Returns 설정 - 라우팅 실패 시 메시지 반환
		factory.setPublisherReturns(true);

		return factory;
	}

	// ===========================
	// RabbitTemplate (Producer)
	// ===========================

	/**
	 * User 전용 RabbitTemplate
	 * - 메시지 직렬화, 재시도 정책, 발행 결과 로그 처리 포함
	 */
	@Bean("userRabbitTemplate")
	public RabbitTemplate userRabbitTemplate(
		@Qualifier("userConnectionFactory") ConnectionFactory connectionFactory,
		MessageConverter messageConverter) {

		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(messageConverter);

		// 라우팅 실패 시 ReturnCallback이 동작하도록 설정
		rabbitTemplate.setMandatory(true);

		// 메시지 지속성 보장 (서버 재시작 시에도 메시지 보존)
		rabbitTemplate.setBeforePublishPostProcessors(message -> {
			message.getMessageProperties().getHeaders().put("delivery-mode", 2); // PERSISTENT
			return message;
		});

		// --- Retry 설정 (지수 백오프) ---
		RetryTemplate retryTemplate = new RetryTemplate();
		ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
		backOffPolicy.setInitialInterval(500);
		backOffPolicy.setMultiplier(2);
		backOffPolicy.setMaxInterval(10_000);
		retryTemplate.setBackOffPolicy(backOffPolicy);
		rabbitTemplate.setRetryTemplate(retryTemplate);

		// --- 메시지 발행 성공/실패 확인 콜백 ---
		rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
			if (ack) {
				log.debug("[User] 메시지 발행 성공 - correlationData: {}", correlationData);
			} else {
				log.error("[User] 메시지 발행 실패 - correlationData: {}, cause: {}", correlationData, cause);
			}
		});

		// --- 라우팅 실패 시 로그 출력 ---
		rabbitTemplate.setReturnsCallback(returnedMessage ->
			log.error("[User] 라우팅 실패 - exchange: {}, routingKey: {}, message: {}",
				returnedMessage.getExchange(),
				returnedMessage.getRoutingKey(),
				returnedMessage.getMessage())
		);
		return rabbitTemplate;
	}
}