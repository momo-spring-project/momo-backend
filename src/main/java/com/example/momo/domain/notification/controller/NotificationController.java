package com.example.momo.domain.notification.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.momo.domain.notification.dto.meeting.NotificationMeetingEvent;
import com.example.momo.domain.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	//todo : WebSocket 테스트를 위해 임시 구현 -> 추후 미 사용시 삭제 예정
	@PostMapping("/api/v1/notifications")
	public String sendMeetingNotification(@RequestBody NotificationMeetingEvent command) {
		notificationService.sendNotification(command);  // WebSocket 푸시 실행
		return "전송됨: " + command.content();
	}
}
