package com.example.momo.domain.meeting.event.rabbitmq.config;

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

@Configuration
public class MeetingQueueConfig {

	// 참가자 Queue
	@Bean
	public Queue participantPaymentSuccessQueue() {
		return QueueBuilder.durable(QueueNames.PARTICIPANT_PAYMENT_SUCCESS)
			.withArgument("x-dead-letter-exchange", RabbitExchangeNames.DLX_PARTICIPANT)
			.withArgument("x-dead-letter-routing-key", QueueNames.DLQ_PARTICIPANT)
			.build();
	}

	@Bean
	public Queue participantPaymentFailQueue() {
		return QueueBuilder.durable(QueueNames.PARTICIPANT_PAYMENT_FAIL)
			.withArgument("x-dead-letter-exchange", RabbitExchangeNames.DLX_PARTICIPANT)
			.withArgument("x-dead-letter-routing-key", QueueNames.DLQ_PARTICIPANT)
			.build();
	}

	// 참가자 DLQ
	@Bean
	public Queue participantDlq() {
		return QueueBuilder.durable(QueueNames.DLQ_PARTICIPANT)
			.build();
	}

	// 참가자 Binding
	@Bean
	public Binding participantPaymentSuccessBinding() {
		return BindingBuilder.bind(participantPaymentSuccessQueue())
			.to(new TopicExchange(RabbitExchangeNames.PARTICIPANT_EVENTS))
			.with("payment.completed"); // 이후에 글로벌 상수로 변경
	}

	@Bean
	public Binding participantPaymentFailBinding() {
		return BindingBuilder.bind(participantPaymentFailQueue())
			.to(new TopicExchange(RabbitExchangeNames.PARTICIPANT_EVENTS))
			.with("payment.failed"); // 이후에 글로벌 상수로 변경
	}

	// 참가자 DLQ 바인딩
	@Bean
	public Binding participantDlqBinding()  {
		return BindingBuilder.bind(participantDlq())
			.to(new  DirectExchange(RabbitExchangeNames.DLX_PARTICIPANT))
			.with(QueueNames.DLQ_PARTICIPANT);
	}


	// ===========================
	// Payment 이벤트 수신 Queue
	// ===========================

	/**
	 * 결제 완료 이벤트 Queue
	 * Payment에서 결제가 완료되면 참가자를 실제로 등록
	 */
	@Bean
	public Queue meetingPaymentCompletedQueue() {
		return QueueBuilder.durable("meeting.payment.completed.queue")
			.withArgument("x-dead-letter-exchange", "momo.dlx.meeting")
			.withArgument("x-dead-letter-routing-key", "meeting.dlq")
			.build();
	}

	/**
	 * 결제 실패 이벤트 Queue
	 * Payment에서 결제가 실패하면 예약한 자리를 복구
	 */
	@Bean
	public Queue meetingPaymentFailedQueue() {
		return QueueBuilder.durable("meeting.payment.failed.queue")
			.withArgument("x-dead-letter-exchange", "momo.dlx.meeting")
			.withArgument("x-dead-letter-routing-key", "meeting.dlq")
			.build();
	}

	/**
	 * Meeting DLQ (Dead Letter Queue)
	 */
	@Bean
	public Queue meetingDlq() {
		return QueueBuilder.durable("meeting.dlq.queue")
			.withArgument("x-message-ttl", 604800000)  // 7일 후 자동 삭제
			.build();
	}

	// ===========================
	// Payment 이벤트 Bindings
	// ===========================

	/**
	 * 결제 완료 이벤트 바인딩
	 * Exchange: momo.payment.events (TopicExchange)
	 * RoutingKey: payment.completed
	 */
	@Bean
	public Binding meetingPaymentCompletedBinding() {
		return BindingBuilder
			.bind(meetingPaymentCompletedQueue())
			.to(new TopicExchange(RabbitExchangeNames.PAYMENT_EVENTS))
			.with(RoutingKeys.PAYMENT_COMPLETED);
	}

	/**
	 * 결제 실패 이벤트 바인딩
	 * Exchange: momo.payment.events (TopicExchange)
	 * RoutingKey: payment.failed
	 */
	@Bean
	public Binding meetingPaymentFailedBinding() {
		return BindingBuilder
			.bind(meetingPaymentFailedQueue())
			.to(new TopicExchange(RabbitExchangeNames.PAYMENT_EVENTS))
			.with(RoutingKeys.PAYMENT_FAILED);
	}
}
