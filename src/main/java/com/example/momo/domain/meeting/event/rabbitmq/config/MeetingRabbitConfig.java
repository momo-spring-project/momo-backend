package com.example.momo.domain.meeting.event.rabbitmq.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.support.RetryTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class MeetingRabbitConfig {
	// ===========================
	// ConnectionFactory
	// ===========================

	/**
	 * Participant 전용 ConnectionFactory
	 * - 글로벌 ConnectionFactory의 커넥션을 복사하여 사용
	 * - 메시지 발행 성공/실패 여부를 확인할 수 있도록 Confirm/Return 기능 활성화
	 */
	@Bean("participantConnectionFactory")
	public ConnectionFactory participantConnectionFactory(CachingConnectionFactory base) {
		CachingConnectionFactory factory =
			new CachingConnectionFactory(base.getRabbitConnectionFactory());

		factory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
		factory.setPublisherReturns(true);
		return factory;
	}

	// ===========================
	// RabbitTemplate (Producer)
	// ===========================

	/**
	 * Participant 전용 RabbitTemplate
	 * - 메시지 직렬화, 재시도 정책, 발행 결과 로그 처리 포함
	 */
	@Bean("participantRabbitTemplate")
	public RabbitTemplate participantRabbitTemplate(
		@Qualifier("participantConnectionFactory") ConnectionFactory connectionFactory,
		MessageConverter messageConverter) {

		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(messageConverter);

		// 라우팅 실패 시 ReturnCallback이 동작하도록 설정
		template.setMandatory(true);

		// --- Retry 설정 (지수 백오프) ---
		RetryTemplate retry = new RetryTemplate();
		ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
		backOff.setInitialInterval(500);   // 초기 대기 시간: 0.5초
		backOff.setMultiplier(2.0);        // 대기 시간 배수: 2배씩 증가
		backOff.setMaxInterval(1_000);    // 최대 대기 시간: 10초, 테스트중 변경: 1초
		retry.setBackOffPolicy(backOff);
		template.setRetryTemplate(retry);
		// Confirm/Return Callback (로깅용)
		template.setConfirmCallback((correlationData, ack, cause) -> {
			if (!ack) {
				log.error("[Participant] Confirm 실패 - correlationId: {}, cause: {}",
					correlationData != null ? correlationData.getId() : "null", cause);
			}
		});

		// --- 라우팅 실패 시 로그 출력 ---
		template.setReturnsCallback(ret -> log.error(
			"[Participant] 라우팅 실패 - exchange: {}, routingKey: {}, message: {}",
			ret.getExchange(), ret.getRoutingKey(), ret.getMessage()));

		return template;
	}

	// ===========================
	// ListenerContainerFactory (Consumer)
	// ===========================

	/**
	 * Participant 전용 ListenerContainerFactory
	 * - 소비자 측 수신 처리, 스레드 수, ACK 전략, 재시도 설정 포함
	 */
	@Bean("participantListenerContainerFactory")
	public SimpleRabbitListenerContainerFactory participantListenerContainerFactory(
		@Qualifier("participantConnectionFactory") ConnectionFactory connectionFactory,
		MessageConverter messageConverter) {

		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(messageConverter);

		// --- 수동 ACK 및 예외 발생 시 DLQ 전송 ---
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);  // 수동 ACK 모드
		factory.setDefaultRequeueRejected(false);            // 예외 발생 시 재큐잉 금지 -> DLQ로 이동

		// Consumer 재시도 설정
		RetryOperationsInterceptor retry = RetryInterceptorBuilder.stateless()
			.maxAttempts(3)
			.backOffOptions(1000, 2.0, 5000)       // 1s -> 2s -> 4s (최대 5초)
			.recoverer(new RejectAndDontRequeueRecoverer()) // 실패 시 메시지 버림 -> DLQ
			.build();

		factory.setAdviceChain(retry);

		return factory;
	}
}