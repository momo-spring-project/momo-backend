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

	// Notification 배달 예정
	@Bean
	public Queue notificationParticipantJoinedQueue() {
		return QueueBuilder.durable(QueueNames.NOTIFICATION_PARTICIPANT_JOIN)
			.build();
	}

	@Bean
	public Queue notificationParticipantCanceledQueue() {
		return QueueBuilder.durable(QueueNames.NOTIFICATION_PARTICIPANT_CANCEL)
			.build();
	}

	@Bean
	public Binding notificationParticipantJoinedBinding() {
		return BindingBuilder.bind(notificationParticipantJoinedQueue())
			.to(new DirectExchange(RabbitExchangeNames.PARTICIPANT_EVENTS))
			.with(RoutingKeys.PARTICIPANT_JOIN);
	}

	@Bean
	public Binding notificationParticipantCanceledBinding() {
		return BindingBuilder.bind(notificationParticipantCanceledQueue())
			.to(new DirectExchange(RabbitExchangeNames.PARTICIPANT_EVENTS))
			.with(RoutingKeys.PARTICIPANT_CANCEL_NOTIFICATION);
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

	/**
	 * Meeting DLQ 바인딩
	 * Bean으로 등록된 DLX를 주입받아 사용
	 */
	@Bean
	public Binding meetingDlqBinding(DirectExchange meetingDlxExchange) {
		return BindingBuilder
			.bind(meetingDlq())
			.to(meetingDlxExchange)
			.with("meeting.dlq");
	}
}
