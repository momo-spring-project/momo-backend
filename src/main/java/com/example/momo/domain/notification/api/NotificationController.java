package com.example.momo.domain.notification.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.momo.domain.auth.domain.dto.AuthUser;
import com.example.momo.domain.notification.application.NotificationService;
import com.example.momo.domain.notification.domain.dto.NotificationDto;
import com.example.momo.domain.notification.domain.dto.NotificationResponseDto;
import com.example.momo.global.common.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping("/notifications")
	public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getNotifications(
		@AuthenticationPrincipal AuthUser authUser) {
		return ResponseEntity.ok(
			ApiResponse.success("알림 정보 조회.", notificationService.getNotifications(authUser.getId())));
	}

	//외부 저장용 메서드
	@PostMapping("/notifications")
	public ResponseEntity<ApiResponse<Void>> createNotification(
		@RequestBody NotificationDto dto) {
		notificationService.createNotification(dto);
		return ResponseEntity.ok(ApiResponse.success("알림 정보 생성", null));
	}

	//외부 전송용 메서드
	@PostMapping("/notifications/send")
	public ResponseEntity<ApiResponse<Void>> sendToUser(@RequestBody NotificationDto dto) {
		notificationService.sendNotification(dto);
		return ResponseEntity.ok(ApiResponse.success("전송됨 : " + dto.content(), null));
	}
}
