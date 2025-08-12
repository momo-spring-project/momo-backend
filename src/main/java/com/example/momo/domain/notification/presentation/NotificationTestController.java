package com.example.momo.domain.notification.presentation;

import static com.example.momo.global.rabbitmq.constant.EventTypeNames.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.momo.global.rabbitmq.constant.EventTypeNames;
import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;
import com.example.momo.global.rabbitmq.constant.RoutingKeys;
import com.example.momo.global.rabbitmq.dto.common.EventWrapper;
import com.example.momo.global.rabbitmq.dto.meeting.MeetingAlarmMessages;

import lombok.RequiredArgsConstructor;

//todo : 테스트 전용 컨트롤러. 삭제 예정
@RestController
@RequiredArgsConstructor
@RequestMapping("/test/notification")
public class NotificationTestController {
	private final RabbitTemplate rabbitTemplate;

	@PostMapping("/test-message")
	public ResponseEntity<Void> testRabbitMQ(@RequestBody MessageTestDto dto) {

		EventWrapper<?> eventWrapper = EventWrapper.of(PAYMENT_COMPLETED, dto);

		publishWrapper(eventWrapper);
		return ResponseEntity.accepted().build();
	}

	@PostMapping("/test-redis")
	public ResponseEntity<Void> testRedisAlarms(@RequestBody RedisTestDto dto) {
		LocalDateTime baseTime = LocalDateTime.now().plusMinutes(dto.minute); // 10초 후 알림

		for (int i = 1; i <= dto.count; i++) {
			List<Long> userIdList = List.of(
				i * 100L + 1,
				i * 100L + 2,
				i * 100L + 3
			);
			publish(new MeetingAlarmMessages.Update(
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

	public record MessageTestDto(
		Long userId,
		Long paymentId
	) {
	}

	public void publish(MeetingAlarmMessages.Update event) {
		EventWrapper<?> eventWrapper = EventWrapper.of(EventTypeNames.MEETING_UPDATE, event);
		rabbitTemplate.convertAndSend(
			RabbitExchangeNames.MEETING_EVENTS,
			RoutingKeys.MEETING_UPDATE_KEY,
			eventWrapper
		);
	}

	public <T> void publishWrapper(EventWrapper<T> event) {
		rabbitTemplate.convertAndSend(
			RabbitExchangeNames.PAYMENT_EVENTS,
			RoutingKeys.PAYMENT_COMPLETED_KEY,
			event
		);
	}
}
