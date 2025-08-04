package com.example.momo.domain.notification.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.momo.global.rabbitMQ.producer.HubMessagePublisher;
import com.example.momo.global.springEvent.meeting.MeetingMessageEvents;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test/notification")
public class NotificationTestController {

	private final HubMessagePublisher publisher;

	@PostMapping
	public ResponseEntity<Void> testRabbitMQ(@RequestBody MeetingUpdateTestDto req) {
		publisher.publish(new MeetingMessageEvents.Update(
			req.meetingId,
			req.meetingName,
			req.userIdList
		));
		return ResponseEntity.accepted().build(); // 202
	}

	public record MeetingUpdateTestDto(
		Long meetingId,
		String meetingName,
		List<Long> userIdList
	) {
	}
}
