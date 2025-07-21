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

	//외부 저장용 메서드
	@PostMapping("/notifications")
	public ResponseEntity<ApiResponse<Void>> saveNotification(
		@RequestBody NotificationMeetingEvent event) {
		notificationService.saveNotification(event);
		return ResponseEntity.ok(ApiResponse.success("알림 정보 저장", null));
	}

	//외부 전송용 메서드
	@PostMapping("/notifications/send")
	public ResponseEntity<ApiResponse<Void>> sendToUser(@RequestBody NotificationMeetingEvent event) {
		notificationService.sendNotification(event);
		return ResponseEntity.ok(ApiResponse.success("전송됨 : " + event.content(), null));
	}
}
