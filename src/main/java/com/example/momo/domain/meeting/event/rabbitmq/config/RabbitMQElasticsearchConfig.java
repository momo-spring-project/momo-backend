package com.example.momo.domain.meeting.event.rabbitmq.config;

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
public class RabbitMQElasticsearchConfig {

	public static final String EXCHANGE_NAME = "elasticsearch.exchange";
	public static final String DLX_SAVED_EXCHANGE_NAME = "elasticsearch.saved.dlx.exchange";
	public static final String DLX_DELETED_EXCHANGE_NAME = "elasticsearch.deleted.dlx.exchange";

	public static final String SAVED_ROUTING_KEY = "elasticsearch.saved.key";
	public static final String DELETED_ROUTING_KEY = "elasticsearch.deleted.key";
	public static final String DLX_SAVED_ROUTING_KEY = "elasticsearch.saved.dlx.key";
	public static final String DLX_DELETED_ROUTING_KEY = "elasticsearch.deleted.dlx.key";

	public static final String SAVED_QUEUE = "elasticsearch.saved.queue";
	public static final String DELETED_QUEUE = "elasticsearch.deleted.queue";
	public static final String SAVED_DEAD_QUEUE = "elasticsearch.saved.dead.queue";
	public static final String DELETED_DEAD_QUEUE = "elasticsearch.deleted.dead.queue";

	@Bean
	public TopicExchange elasticsearchExchange() {
		return new TopicExchange(EXCHANGE_NAME);
	}

	@Bean
	public Queue elasticsearchSavedQueue() {
		return QueueBuilder.durable(SAVED_QUEUE)
			.withArgument("x-dead-letter-exchange", DLX_SAVED_EXCHANGE_NAME)
			.withArgument("x-dead-letter-routing-key", DLX_SAVED_ROUTING_KEY)
			.withArgument("x-message-ttl", 10000)
			.build();
	}

	@Bean
	public Queue elasticsearchDeletedQueue() {
		return QueueBuilder.durable(DELETED_QUEUE)
			.withArgument("x-dead-letter-exchange", DLX_DELETED_EXCHANGE_NAME)
			.withArgument("x-dead-letter-routing-key", DLX_DELETED_ROUTING_KEY)
			.withArgument("x-message-ttl", 10000)
			.build();
	}

	@Bean
	public TopicExchange deadLetterSavedExchange() {
		return new TopicExchange(DLX_SAVED_EXCHANGE_NAME);
	}

	@Bean
	public TopicExchange deadLetterDeletedExchange() {
		return new TopicExchange(DLX_DELETED_EXCHANGE_NAME);
	}

	@Bean
	public Queue deadLetterSavedQueue() {
		return new Queue(SAVED_DEAD_QUEUE, true);
	}

	@Bean
	public Queue deadLetterDeletedQueue() {
		return new Queue(DELETED_DEAD_QUEUE, true);
	}

	@Bean
	public Binding deadLetterSavedBinding() {
		return BindingBuilder
			.bind(deadLetterSavedQueue())
			.to(deadLetterSavedExchange())
			.with(DLX_SAVED_ROUTING_KEY);
	}

	@Bean
	public Binding deadLetterDeletedBinding() {
		return BindingBuilder
			.bind(deadLetterDeletedQueue())
			.to(deadLetterDeletedExchange())
			.with(DLX_DELETED_ROUTING_KEY);
	}

	@Bean
	public Binding elasticsearchSavedBinding() {
		return BindingBuilder.bind(elasticsearchSavedQueue()).to(elasticsearchExchange()).with(SAVED_ROUTING_KEY);
	}

	@Bean
	public Binding elasticsearchDeletedBinding() {
		return BindingBuilder.bind(elasticsearchDeletedQueue()).to(elasticsearchExchange()).with(DELETED_ROUTING_KEY);
	}

	/**
	 * ack AUTO : 설정을 추가로 하지 않아도 AUTO이지만 명시하기 위해 사용
	 * prefetch : 5개 병렬 처리 (부하 조절 하기 위함)
	 */
	@Bean("elasticsearchListenerContainerFactory")
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
		ConnectionFactory connectionFactory,
		MessageConverter messageConverter
	) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(messageConverter);
		factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
		factory.setPrefetchCount(5);

		// 재시도 인터셉터
		factory.setAdviceChain(retryInterceptor());

		return factory;
	}

	@Bean
	public RetryOperationsInterceptor retryInterceptor() {
		return RetryInterceptorBuilder
			.stateless()
			.maxAttempts(3)
			.backOffOptions(1000, 2.0, 10000)
			.recoverer(new RejectAndDontRequeueRecoverer())
			.build();
	}
}
