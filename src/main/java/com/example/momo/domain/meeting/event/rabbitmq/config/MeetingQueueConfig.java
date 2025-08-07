package com.example.momo.domain.meeting.event.rabbitmq.config;

import com.example.momo.global.rabbitmq.constant.QueueNames;
import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
