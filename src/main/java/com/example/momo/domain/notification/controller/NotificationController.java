package com.example.momo.domain.notification.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.momo.domain.auth.dto.AuthUser;
import com.example.momo.domain.common.dto.ApiResponse;
import com.example.momo.domain.notification.domain.NotificationResponse;
import com.example.momo.domain.notification.service.NotificationService;
import com.example.momo.global.event.NotificationMeetingEvent;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping("/notifications")
	public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
		@AuthenticationPrincipal AuthUser authUser) {
		return ResponseEntity.ok(
			ApiResponse.success("알림 정보 조회.", notificationService.getNotifications(authUser.getId())));
	}

	@PostMapping("/notifications")
	public ResponseEntity<ApiResponse<NotificationResponse>> saveNotification(
		@RequestBody NotificationMeetingEvent event) {
		return ResponseEntity.ok(ApiResponse.success("알림 정보 저장", notificationService.saveNotification(event)));
	}

	@PostMapping("/admin/notifications")
	public ResponseEntity<String> sendToUser(@RequestBody NotificationMeetingEvent event) {
		notificationService.sendNotification(event);  // WebSocket 푸시 실행
		return ResponseEntity.ok("전송됨: " + event.content());
	}
}
