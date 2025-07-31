package com.example.momo.global.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	public static final String QUEUE_NAME = "momo.queue";
	public static final String EXCHANGE_NAME = "momo.exchange";
	public static final String ROUTING_KEY = "momo.key";

	//durable queue (서버 재시작에도 유지됨)
	//큐 메타데이터(이름,ttl,바인딩 등)이 유지됨
	//큐 안에 아직 소비되지 않은 메세지들도 유지됨

	@Bean
	public Queue queue() {
		return QueueBuilder.durable(QUEUE_NAME)
			.withArgument("x-dead-letter-exchange", "momo.dlx")  //DLX 지정
			//실패 시 메세지를 보낼 교환기
			.withArgument("x-dead-letter-routing-key", "momo.dlx.key")
			//dlq 라우팅 키
			.withArgument("x-message-ttl", 10000)  //10초 후 만료
			//메세지가 큐에 도달하고 처리되지 않으면 ttl 시간 후 dlq로 이동
			.build();
	}

	//DLX (Dead Letter Exchange)
	@Bean
	public TopicExchange deadLetterExchange() {
		return new TopicExchange("momo.dlx");
	}

	//DLQ (실패 메시지 수신용 큐)
	@Bean
	public Queue deadLetterQueue() {
		return new Queue("momo.dlq", true);
	}

	//DLQ Binding
	@Bean
	public Binding deadLetterBinding() {
		return BindingBuilder
			.bind(deadLetterQueue())
			.to(deadLetterExchange())
			.with("momo.dlx.key");
	}

	//topic exchange 생성
	@Bean
	public TopicExchange exchange() {
		return new TopicExchange(EXCHANGE_NAME);
	}

	//queue와 exchange를 routing key로 연결
	@Bean
	public Binding binding() {
		return BindingBuilder
			.bind(queue())
			.to(exchange())
			.with("momo.#");
	}

	//json 메세지 컨버터
	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	//rabbitTemplate에 컨버터 적용
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(jsonMessageConverter());
		return template;
	}
}
