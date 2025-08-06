package com.example.momo.domain.payment.event.rabbitmq.config;

import com.example.momo.global.rabbitmq.constant.QueueNames;
import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;
import com.example.momo.global.rabbitmq.constant.RoutingKeys;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**

 Payment 도메인이 소비하는 Queue 정의
 */
@Configuration
public class PaymentQueueConfig {

	// Meeting 도메인의 Exchange (이미 존재하는 전역 exchange라고 가정)
	private static final String X_MEETING_EVENTS = "momo.meeting.events";

	/**
	 * Meeting 도메인의 참가자 생성 이벤트를 수신하는 큐
	 */
	@Bean
	public Queue paymentParticipantCreatedQueue() {
		return QueueBuilder.durable("payment.participant.created.queue")
			.withArgument("x-dead-letter-exchange", PaymentExchangeConfig.X_DLX_PAYMENT)
			.withArgument("x-dead-letter-routing-key", "payment.dlq")
			.build();
	}

	/**
	 * 공통 DLQ
	 */
	@Bean
	public Queue paymentDlq() {
		return QueueBuilder.durable("payment.dlq")
			.build();
	}

	/**
	 * Meeting 도메인의 참가자 생성 이벤트 바인딩
	 */
	@Bean
	public Binding participantCreatedBinding() {
		return BindingBuilder.bind(paymentParticipantCreatedQueue())
			.to(new TopicExchange(X_MEETING_EVENTS))//전역 exchange라고 가정
			.with("meeting.participant.created");
	}

	@Bean
	public Binding dlqBinding() {
		return BindingBuilder.bind(paymentDlq())
			.to(new DirectExchange(PaymentExchangeConfig.X_DLX_PAYMENT))
			.with("payment.dlq");
	}

	/**
	 *
	 * Participant 에서 만든 기본 큐 배달
	 */
	@Bean
	public Queue paymentParticipantRegisteredQueue() {
		return QueueBuilder.durable(QueueNames.PAYMENT_PARTICIPANT_REGISTER)
			.build();
	}

	@Bean
	public Queue paymentParticipantCanceledQueue() {
		return QueueBuilder.durable(QueueNames.PAYMENT_PARTICIPANT_CANCEL)
			.build();
	}

	@Bean
	public Binding paymentParticipantRegisteredBinding() {
		return BindingBuilder.bind(paymentParticipantRegisteredQueue())
			.to(new DirectExchange(RabbitExchangeNames.PARTICIPANT_EVENTS))
			.with(RoutingKeys.PARTICIPANT_REGISTER);
	}

	@Bean
	public Binding paymentParticipantCanceledBinding() {
		return BindingBuilder.bind(paymentParticipantCanceledQueue())
			.to(new DirectExchange(RabbitExchangeNames.PARTICIPANT_EVENTS))
			.with(RoutingKeys.PARTICIPANT_CANCEL_REFUND);
	}
}