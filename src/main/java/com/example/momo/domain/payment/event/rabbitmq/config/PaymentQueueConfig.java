package com.example.momo.domain.payment.event.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.momo.global.rabbitmq.constant.QueueNames;
import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;
import com.example.momo.global.rabbitmq.constant.RoutingKeys;

/**

 Payment 도메인이 소비하는 Queue 정의
 */
@Configuration
public class PaymentQueueConfig {

	// ===========================
	// Payment 도메인 전용 Queue
	// ===========================

	/**
	 * 참가자 등록 이벤트를 수신하는 큐
	 * - Participant 도메인에서 participant.registered 이벤트 발행
	 * - 결제 처리 시작
	 */
	@Bean
	public Queue paymentParticipantRegisteredQueue() {
		return QueueBuilder.durable(QueueNames.PAYMENT_PARTICIPANT_REGISTER)
			.withArgument("x-dead-letter-exchange", RabbitExchangeNames.DLX_PAYMENT)
			.withArgument("x-dead-letter-routing-key", RoutingKeys.PAYMENT_DLQ)
			.build();
	}

	/**
	 * 참가자 취소(환불) 이벤트를 수신하는 큐
	 * - Participant 도메인에서 participant.canceled.refund 이벤트 발행
	 * - 환불 처리
	 */
	@Bean
	public Queue paymentParticipantCanceledQueue() {
		return QueueBuilder.durable(QueueNames.PAYMENT_PARTICIPANT_CANCEL)
			.withArgument("x-dead-letter-exchange", RabbitExchangeNames.DLX_PAYMENT)
			.withArgument("x-dead-letter-routing-key", RoutingKeys.PAYMENT_DLQ)
			.build();
	}

	@Bean
	public Queue paymentMeetingDeletedQueue() {
		return QueueBuilder.durable("payment.meeting.deleted.queue")
			.withArgument("x-dead-letter-exchange", RabbitExchangeNames.DLX_PAYMENT)
			.withArgument("x-dead-letter-routing-key", RoutingKeys.PAYMENT_DLQ)
			.build();
	}

	/**
	 * Payment 도메인 공통 DLQ
	 * - 모든 실패 메시지가 모이는 곳
	 * - 재시도 3회 초과 시 이동
	 */
	@Bean
	public Queue paymentDlq() {
		return QueueBuilder.durable(QueueNames.PAYMENT_DLQ)
			.withArgument("x-message-ttl", 604800000)  // 7일 후 자동 삭제
			.build();
	}

	// ===========================
	// Bindings
	// ===========================

	/**
	 * 참가자 등록 이벤트 바인딩
	 * Exchange: momo.participant.events (DirectExchange)
	 * RoutingKey: participant.registered
	 */
	@Bean
	public Binding paymentParticipantRegisteredBinding() {
		return BindingBuilder
			.bind(paymentParticipantRegisteredQueue())
			.to(new DirectExchange(RabbitExchangeNames.PARTICIPANT_EVENTS))
			.with(RoutingKeys.PARTICIPANT_REGISTER);
	}

	/**
	 * 참가자 취소 이벤트 바인딩
	 * Exchange: momo.participant.events (DirectExchange)
	 * RoutingKey: participant.canceled.refund
	 */
	@Bean
	public Binding paymentParticipantCanceledBinding() {
		return BindingBuilder
			.bind(paymentParticipantCanceledQueue())
			.to(new DirectExchange(RabbitExchangeNames.PARTICIPANT_EVENTS))
			.with(RoutingKeys.PARTICIPANT_CANCEL_REFUND);
	}

	@Bean
	public Binding meetingDeletedBinding() {
		return BindingBuilder
			.bind(paymentMeetingDeletedQueue())
			.to(new TopicExchange(RabbitExchangeNames.MEETING_EVENTS))       // meeting.exchange
			.with(RoutingKeys.MEETING_DELETE);                 // meeting.deleted
	}

	/**
	 * DLQ 바인딩
	 * Exchange: dlx.payment (DirectExchange)
	 * RoutingKey: payment.dlq
	 */
	@Bean
	public Binding paymentDlqBinding() {
		return BindingBuilder
			.bind(paymentDlq())
			.to(new DirectExchange(RabbitExchangeNames.DLX_PAYMENT))
			.with(RoutingKeys.PAYMENT_DLQ);
	}

	// ===========================
	// 추후 구현 예정 Queue
	// ===========================

	/**
	 * 모임 삭제 이벤트 큐 (추후 구현)
	 * - 모임이 삭제되면 모든 참가자의 결제를 환불
	 *
	 * @Bean
	 * public Queue paymentMeetingDeletedQueue() {
	 *     return QueueBuilder.durable(QueueNames.PAYMENT_MEETING_DELETED)
	 *         .withArgument("x-dead-letter-exchange", RabbitExchangeNames.X_DLX_PAYMENT)
	 *         .withArgument("x-dead-letter-routing-key", RoutingKeys.PAYMENT_DLQ)
	 *         .build();
	 * }
	 *
	 * @Bean
	 * public Binding paymentMeetingDeletedBinding() {
	 *     return BindingBuilder
	 *         .bind(paymentMeetingDeletedQueue())
	 *         .to(new DirectExchange(RabbitExchangeNames.MEETING_EVENTS))
	 *         .with(RoutingKeys.MEETING_DELETED);
	 * }
	 */
}