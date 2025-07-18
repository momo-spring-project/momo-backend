package com.example.momo.domain.notification.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.momo.domain.notification.dto.NotificationResponse;
import com.example.momo.domain.notification.service.NotificationService;
import com.example.momo.global.event.NotificationMeetingEvent;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping("/notifications")
	public ResponseEntity<List<NotificationResponse>> getNotifications() {
		Long userId = 1L;
		List<NotificationResponse> responseList = notificationService.getNotifications(userId);
		return ResponseEntity.ok(responseList);
	}

	@PostMapping("/notifications")
	public ResponseEntity<String> saveNotification(@RequestBody NotificationMeetingEvent command) {
		notificationService.saveNotification(command);
		return ResponseEntity.ok("저장완료");
	}

	@PostMapping("/users/{userId}/notifications")
	public ResponseEntity<String> sendToUser(@RequestBody NotificationMeetingEvent command) {
		notificationService.sendNotification(command);  // WebSocket 푸시 실행
		return ResponseEntity.ok("전송됨: " + command.content());
	}
}
