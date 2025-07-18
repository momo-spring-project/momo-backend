package com.example.momo.domain.notification.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.momo.domain.notification.dto.meeting.NotificationMeetingCommand;
import com.example.momo.domain.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@PostMapping("/api/v1/notifications")
	public String sendMeetingNotification(@RequestBody NotificationMeetingCommand command) {
		notificationService.sendNotification(command);  // WebSocket 푸시 실행
		return "전송됨: " + command.content();
	}
}
