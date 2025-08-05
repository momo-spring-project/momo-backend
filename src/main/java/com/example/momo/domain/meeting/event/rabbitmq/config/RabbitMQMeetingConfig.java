package com.example.momo.domain.meeting.event.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// TODO : 이 부분 DLQ 관리를 어떤 방식으로 해야할지? 지금 허브쪽에서는 로그로 처리
@Configuration
public class RabbitMQMeetingConfig {

	public static final String EXCHANGE_NAME = "meeting.exchange";

	public static final String CREATED_ROUTING_KEY = "meeting.created";
	public static final String UPDATED_ROUTING_KEY = "meeting.updated";
	public static final String DELETED_ROUTING_KEY = "meeting.deleted";
	public static final String STATUS_ROUTING_KEY = "meeting.status";

	public static final String CREATED_QUEUE = "notification.meeting.created.queue";
	public static final String UPDATED_QUEUE = "notification.meeting.updated.queue";
	public static final String DELETED_QUEUE = "notification.meeting.deleted.queue";
	public static final String STATUS_QUEUE = "notification.meeting.status.queue";

	@Bean
	public TopicExchange meetingExchange() {
		return new TopicExchange(EXCHANGE_NAME);
	}

	@Bean
	public Queue meetingCreateQueue() {
		return QueueBuilder.durable(CREATED_QUEUE).build();
	}

	@Bean
	public Queue meetingUpdateQueue() {
		return QueueBuilder.durable(UPDATED_QUEUE).build();
	}

	@Bean
	public Queue meetingDeleteQueue() {
		return QueueBuilder.durable(DELETED_QUEUE).build();
	}

	@Bean
	public Queue meetingStatusQueue() {
		return QueueBuilder.durable(STATUS_QUEUE).build();
	}

	@Bean
	public Binding createdBinding() {
		return BindingBuilder.bind(meetingCreateQueue()).to(meetingExchange()).with(CREATED_ROUTING_KEY);
	}

	@Bean
	public Binding updatedBinding() {
		return BindingBuilder.bind(meetingUpdateQueue()).to(meetingExchange()).with(UPDATED_ROUTING_KEY);
	}

	@Bean
	public Binding deletedBinding() {
		return BindingBuilder.bind(meetingDeleteQueue()).to(meetingExchange()).with(DELETED_ROUTING_KEY);
	}

	@Bean
	public Binding statusBinding() {
		return BindingBuilder.bind(meetingStatusQueue()).to(meetingExchange()).with(STATUS_ROUTING_KEY);
	}
}
