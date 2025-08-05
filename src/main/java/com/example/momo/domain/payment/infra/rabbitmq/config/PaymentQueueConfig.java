package com.example.momo.domain.payment.infra.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Payment 도메인이 소비하는 Queue 정의
 */
@Configuration
public class PaymentQueueConfig {

	// Queue 이름
	public static final String Q_PAYMENT_FAILED_HOLD = "payment.failed.hold.queue";
	public static final String Q_PAYMENT_HOLD_TIMEOUT_DLQ = "payment.hold.timeout.dlq";
	public static final String Q_PAYMENT_PARTICIPANT_CREATED = "payment.participant.created.queue";

	// Meeting 도메인의 Exchange
	private static final String X_MEETING_EVENTS = "x.meeting.events";

	/**
	 * 결제 실패 후 30분 보류 큐
	 * FAILED 상태 결제에 대한 자리 보류 처리
	 */
	@Bean
	public Queue paymentFailedHoldQueue() {
		return QueueBuilder.durable(Q_PAYMENT_FAILED_HOLD)
			.withArgument("x-message-ttl", 1800000) // 30분
			.withArgument("x-dead-letter-exchange", PaymentExchangeConfig.X_DLX_PAYMENT)
			.withArgument("x-dead-letter-routing-key", "payment.hold.expired")
			.build();
	}

	/**
	 * 보류 타임아웃 DLQ
	 * 30분 후 자리 복원을 위한 이벤트 발행
	 */
	@Bean
	public Queue paymentHoldTimeoutDlq() {
		return QueueBuilder.durable(Q_PAYMENT_HOLD_TIMEOUT_DLQ)
			.build();
	}

	/**
	 * Meeting 도메인의 참가자 생성 이벤트를 수신하는 큐
	 */
	@Bean
	public Queue paymentParticipantCreatedQueue() {
		return QueueBuilder.durable(Q_PAYMENT_PARTICIPANT_CREATED)
			.build();
	}

	// Bindings
	@Bean
	public Binding failedHoldBinding() {
		return BindingBuilder.bind(paymentFailedHoldQueue())
			.to(new DirectExchange(PaymentExchangeConfig.X_PAYMENT_DELAY))
			.with("payment.failed.hold");
	}

	@Bean
	public Binding holdTimeoutDlqBinding() {
		return BindingBuilder.bind(paymentHoldTimeoutDlq())
			.to(new DirectExchange(PaymentExchangeConfig.X_DLX_PAYMENT))
			.with("payment.hold.expired");
	}

	/**
	 * Meeting 도메인의 참가자 생성 이벤트 바인딩
	 */
	@Bean
	public Binding participantCreatedBinding() {
		return BindingBuilder.bind(paymentParticipantCreatedQueue())
			.to(new TopicExchange(X_MEETING_EVENTS))
			.with("meeting.participant.created");
	}
}