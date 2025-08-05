package com.example.momo.domain.payment.infra.rabbitmq.config;

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

/**
 * RabbitMQ 설정 (Payment 도메인 전용)
 * - 메시지 신뢰성과 안전한 오류 처리를 위한 Producer/Consumer 구성
 */
@Slf4j
@Configuration
public class PaymentRabbitConfig {

	// ===========================
	// ConnectionFactory
	// ===========================

	/**
	 * Payment 전용 ConnectionFactory
	 * - 글로벌 ConnectionFactory의 커넥션을 복사하여 사용
	 * - 메시지 발행 성공/실패 여부를 확인할 수 있도록 Confirm/Return 기능 활성화
	 */
	@Bean("paymentConnectionFactory")
	public ConnectionFactory paymentConnectionFactory(CachingConnectionFactory base) {
		CachingConnectionFactory factory =
			new CachingConnectionFactory(base.getRabbitConnectionFactory());

		factory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
		factory.setPublisherReturns(true);
		//factory.setChannelCacheSize(25) 채널 캐시 최대 수
		return factory;
	}

	// ===========================
	// RabbitTemplate (Producer)
	// ===========================

	/**
	 * Payment 전용 RabbitTemplate
	 * - 메시지 직렬화, 재시도 정책, 발행 결과 로그 처리 포함
	 */
	@Bean("paymentRabbitTemplate")
	public RabbitTemplate paymentRabbitTemplate(
		@Qualifier("paymentConnectionFactory") ConnectionFactory connectionFactory,
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
		backOff.setMaxInterval(10_000);    // 최대 대기 시간: 10초
		retry.setBackOffPolicy(backOff);
		template.setRetryTemplate(retry);

		// --- 메시지 발행 성공/실패 확인 콜백 ---
		template.setConfirmCallback((corr, ack, cause) -> {
			if (!ack) {
				log.error("[Payment] 메시지 발행 실패 - correlationData: {}, cause: {}", corr, cause);
			}
		});

		// --- 라우팅 실패 시 로그 출력 ---
		template.setReturnsCallback(ret -> log.error(
			"[Payment] 라우팅 실패 - exchange: {}, routingKey: {}, message: {}",
			ret.getExchange(), ret.getRoutingKey(), ret.getMessage()));

		return template;
	}

	// ===========================
	// ListenerContainerFactory (Consumer)
	// ===========================

	/**
	 * Payment 전용 ListenerContainerFactory
	 * - 소비자 측 수신 처리, 스레드 수, ACK 전략, 재시도 설정 포함
	 */
	@Bean("paymentListenerContainerFactory")
	public SimpleRabbitListenerContainerFactory paymentListenerContainerFactory(
		@Qualifier("paymentConnectionFactory") ConnectionFactory connectionFactory,
		MessageConverter messageConverter) {

		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(messageConverter);

		// // --- 소비 스레드 및 Prefetch 수 ---
		// factory.setConcurrentConsumers(2);        // 기본 소비 스레드 수
		// factory.setMaxConcurrentConsumers(5);     // 최대 동시 소비자 수
		// factory.setPrefetchCount(5);              // 미리 가져올 메시지 수

		// --- 수동 ACK 및 예외 발생 시 DLQ 전송 ---
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);  // 수동 ACK 모드
		factory.setDefaultRequeueRejected(false);            // 예외 발생 시 재큐잉 금지 -> DLQ로 이동

		// --- 소비자 측 Retry 정책 (3회 시도 후 DLQ) ---
		RetryOperationsInterceptor retry = RetryInterceptorBuilder.stateless()
			.maxAttempts(3)                        // 최대 3회 재시도
			.backOffOptions(1000, 2.0, 5000)       // 1s -> 2s -> 4s (최대 5초)
			.recoverer(new RejectAndDontRequeueRecoverer()) // 실패 시 메시지 버림 -> DLQ
			.build();
		factory.setAdviceChain(retry);

		// --- 에러 핸들러 (로깅) ---
		factory.setErrorHandler(t -> log.error("[Payment] 메시지 처리 중 예외 발생", t));

		return factory;
	}
}
