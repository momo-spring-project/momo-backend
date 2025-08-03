package com.example.momo.domain.notification.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.momo.domain.auth.application.dto.AuthUser;
import com.example.momo.domain.notification.application.NotificationService;
import com.example.momo.domain.notification.application.dto.NotificationRequestDto;
import com.example.momo.domain.notification.application.dto.NotificationResponseDto;
import com.example.momo.global.common.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getNotifications(
		@AuthenticationPrincipal AuthUser authUser) {
		return ResponseEntity.ok(
			ApiResponse.success("알림 정보 조회.", notificationService.getNotifications(authUser.getId())));
	}

	//외부 저장용 메서드
	@PostMapping
	public ResponseEntity<ApiResponse<NotificationResponseDto>> createNotification(
		@RequestBody NotificationRequestDto dto) {
		return ResponseEntity.ok(ApiResponse.success("알림 정보 생성", notificationService.createNotification(dto)));
	}
}
