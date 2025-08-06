package com.example.momo.domain.notification.presentation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.momo.global.rabbitMQ.dto.meeting.MeetingAlarmMessages;
import com.example.momo.global.rabbitMQ.producer.HubMessageProducer;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test/notification")
public class NotificationTestController {

	private final HubMessageProducer publisher;

	@PostMapping
	public ResponseEntity<Void> testRabbitMQ(@RequestBody MeetingUpdateTestDto req) {
		publisher.publish(new MeetingAlarmMessages.Update(
			req.meetingId,
			req.meetingName,
			req.userIdList,
			LocalDateTime.now()
		));
		return ResponseEntity.accepted().build(); // 202
	}

	@PostMapping("/redis-test")
	public ResponseEntity<Void> testRedisAlarms(@RequestBody RedisTestDto dto) {
		LocalDateTime baseTime = LocalDateTime.now().plusMinutes(dto.minute); // 10초 후 알림

		for (int i = 1; i <= dto.count; i++) {
			List<Long> userIdList = List.of(
				i * 100L + 1,
				i * 100L + 2,
				i * 100L + 3
			);
			publisher.publish(new MeetingAlarmMessages.Update(
				(long)i, // userId
				"테스트 모임 " + i,
				userIdList,
				baseTime
			));
		}

		return ResponseEntity.accepted().build(); // 202
	}

	public record RedisTestDto(
		int count,
		int minute
	) {
	}

	public record MeetingUpdateTestDto(
		Long meetingId,
		String meetingName,
		List<Long> userIdList,
		LocalDateTime meetingDate
	) {
	}
}
