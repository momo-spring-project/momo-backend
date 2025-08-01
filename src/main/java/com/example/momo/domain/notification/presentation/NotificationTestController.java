package com.example.momo.domain.notification.presentation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.momo.global.rabbitMQ.producer.HubMessagePublisher;
import com.example.momo.global.springEvent.meeting.MeetingMessageEvents;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test/send")
public class NotificationTestController {

	private final HubMessagePublisher publisher;

	//외부 저장용 메서드
	@PostMapping
	public void testRabbitMQ() {
		publisher.publish(new MeetingMessageEvents.Update(
			1L,
			"Title",
			new ArrayList<>(List.of(1L, 2L, 3L))
		));
	}
}
