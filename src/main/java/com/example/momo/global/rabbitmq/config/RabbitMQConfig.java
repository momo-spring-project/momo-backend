package com.example.momo.global.rabbitmq.config;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
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
 * - 모든 도메인에서 재사용 가능한 기본 설정 정의
 * - JSON 메시지 컨버터, 커넥션 팩토리, RabbitTemplate, 수신 컨테이너 팩토리 포함
 * - 도메인 전용 설정이 없을 경우 해당 Bean이 주입됨
 */
@Slf4j
@EnableRabbit
@Configuration
public class RabbitMQConfig {

	/**
	 * 메시지 직렬화 컨버터
	 * - Java 객체 <-> JSON 간 직렬화/역직렬화 처리
	 * - RabbitTemplate, @RabbitListener 양쪽에 공통으로 사용됨
	 */
	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	/**
	 * 기본 ConnectionFactory 등록
	 * - spring.rabbitmq.* 설정을 바탕으로 CachingConnectionFactory 인스턴스를 생성
	 * - CachingConnectionFactory는 채널을 내부적으로 캐싱하여 성능 향상
	 * - @Primary 설정으로 도메인별 커넥션 팩토리가 없을 경우 이 Bean이 주입됨
	 */
	@Primary
	@Bean
	@ConfigurationProperties(prefix = "spring.rabbitmq")
	public CachingConnectionFactory rabbitConnectionFactory() {
		return new CachingConnectionFactory();
	}

	/**
	 * 기본 RabbitListener 컨테이너 팩토리
	 * - @RabbitListener에서 사용할 메시지 수신용 리스너 컨테이너 구성
	 * - 메시지 컨버터 및 커넥션 팩토리 연결
	 * - 메시지 처리 동시성, ack 모드, prefetch 수 등은 도메인에서 커스터마이징 가능
	 */
	@Primary
	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
		MessageConverter messageConverter) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory); // MQ 연결
		factory.setMessageConverter(messageConverter);  // JSON 직렬화 지원

		// 도메인별 설정 예시 (필요 시 도메인 전용 팩토리에서 별도 구성 가능):
		// factory.setConcurrentConsumers(2);        // 동시에 실행할 수신 스레드 수
		// factory.setMaxConcurrentConsumers(5);     // 최대 동시 수신 스레드 수
		// factory.setPrefetchCount(10);             // 한 번에 가져올 메시지 수
		// factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 수동 ACK 모드

		return factory;
	}

	/**
	 * 기본 RabbitTemplate 등록
	 * - 메시지 발행에 사용되는 핵심 컴포넌트
	 * - 메시지를 JSON으로 직렬화 후 지정된 Exchange와 RoutingKey로 전송
	 * - 기본 Exchange, RoutingKey는 필요 시 템플릿 레벨에서 설정 가능
	 */
	@Primary
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
		MessageConverter messageConverter) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(messageConverter); // JSON 직렬화 적용

		// 선택: 공통 템플릿 수준에서 기본 Exchange 및 RoutingKey 지정 가능
		// template.setExchange("default.exchange");
		// template.setRoutingKey("default.routing.key");
		return template;
	}

}