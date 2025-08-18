package com.example.momo.domain.messagehub.event.rabbitmq.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

/**
 * 메시지 허브 전용 RabbitMQ 리스너 컨테이너 팩토리 설정.
 * 수동 ACK 모드, 동시성 및 Prefetch 설정, 재시도 정책과 메시지 변환기를 구성.
 */
@Configuration
public class MessageHubRabbitConfig {

	@Bean(name = "hubListenerContainerFactory")
	public SimpleRabbitListenerContainerFactory hubListenerContainerFactory(
		ConnectionFactory connectionFactory,
		MessageConverter messageConverter
	) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(messageConverter);
		factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

		factory.setConcurrentConsumers(3);
		factory.setMaxConcurrentConsumers(10);
		factory.setPrefetchCount(50);

		factory.setDefaultRequeueRejected(false);
		RetryOperationsInterceptor retry = RetryInterceptorBuilder.stateless()
			.maxAttempts(5)
			.backOffOptions(200, 2.0, 5000)
			.recoverer(new RejectAndDontRequeueRecoverer())
			.build();
		factory.setAdviceChain(retry);
		return factory;
	}

}
