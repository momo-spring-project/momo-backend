package com.example.momo.domain.meeting.event.rabbitmq.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.domain.MeetingRepository;
import com.example.momo.domain.meeting.event.rabbitmq.config.RabbitMQElasticsearchConfig;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MeetingElasticsearchConsumer {

	private final MeetingRepository meetingRepository;

	@RabbitListener(
		queues = RabbitMQElasticsearchConfig.SAVED_QUEUE,
		containerFactory = "elasticsearchListenerContainerFactory"
	)
	public void createMeeting(Meeting meeting) {

		meetingRepository.saveMeetingElastic(meeting);
	}

	@RabbitListener(
		queues = RabbitMQElasticsearchConfig.DELETED_QUEUE,
		containerFactory = "elasticsearchListenerContainerFactory"
	)
	public void deleteMeeting(Meeting meeting) {

		meetingRepository.deleteMeetingElastic(meeting);
	}
}
