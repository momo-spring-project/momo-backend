package com.example.momo.domain.user.event.rabbitmq.config;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;

/**
 * User 도메인이 발행하는 Exchange 정의
 * - Spring이 이 Bean들을 읽어서 실제 RabbitMQ 서버에 Exchange를 생성함
 */
@Configuration
public class UserExchangeConfig {

	/**
	 * User 이벤트 Exchange (Topic)
	 * - user.withdrawn (유저 탈퇴)
	 * - user.created (유저 생성)
	 * - user.updated (유저 정보 수정)
	 * 등등 유저 관련 모든 이벤트가 이 Exchange로 발행됨
	 *
	 * @return TopicExchange - 패턴 매칭 라우팅 지원 (user.*, user.withdrawn 등)
	 */
	@Bean
	public TopicExchange userEventsExchange() {
		return ExchangeBuilder.topicExchange(RabbitExchangeNames.USER_EVENTS)
			.durable(true)    // 서버 재시작해도 Exchange 유지
			.build();
	}
}