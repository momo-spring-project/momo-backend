package com.example.momo.domain.payment.event.rabbitmq.config;

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

@Configuration
public class PaymentRabbitConfig {

	@Bean("paymentConnectionFactory")
	public ConnectionFactory paymentConnectionFactory(
		@Qualifier("rabbitConnectionFactory") CachingConnectionFactory base) {

		CachingConnectionFactory factory =
			new CachingConnectionFactory(base.getRabbitConnectionFactory());

		factory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
		factory.setPublisherReturns(true);

		return factory;
	}

	@Bean("paymentRabbitTemplate")
	public RabbitTemplate paymentRabbitTemplate(
		@Qualifier("paymentConnectionFactory") ConnectionFactory connectionFactory,
		MessageConverter messageConverter) {

		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(messageConverter);

		// Returns 정보를 브로커에서 돌려받도록
		template.setMandatory(true);

		// 채널/네트워크 오류 재시도 (Confirm/NACK은 콜백에서 처리)
		RetryTemplate retry = new RetryTemplate();
		ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
		backOff.setInitialInterval(500);   // 0.5s
		backOff.setMultiplier(2.0);        // x2
		backOff.setMaxInterval(10_000);    // 10s
		retry.setBackOffPolicy(backOff);
		template.setRetryTemplate(retry);

		return template;
	}

	@Bean("paymentListenerContainerFactory")
	public SimpleRabbitListenerContainerFactory paymentListenerContainerFactory(
		@Qualifier("paymentConnectionFactory") ConnectionFactory connectionFactory,
		MessageConverter messageConverter) {

		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(messageConverter);

		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		factory.setDefaultRequeueRejected(false);

		factory.setPrefetchCount(20);
		factory.setConcurrentConsumers(3);
		factory.setMaxConcurrentConsumers(6);

		RetryOperationsInterceptor retry = RetryInterceptorBuilder.stateless()
			.maxAttempts(3)
			.backOffOptions(1000, 2.0, 5000) // 1s -> 2s -> 4s
			.recoverer(new RejectAndDontRequeueRecoverer())
			.build();
		factory.setAdviceChain(retry);

		return factory;
	}
}
