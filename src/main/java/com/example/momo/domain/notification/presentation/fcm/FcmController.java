package com.example.momo.domain.notification.presentation.fcm;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.momo.domain.auth.application.dto.AuthUser;
import com.example.momo.domain.notification.application.fcm.FcmService;
import com.example.momo.domain.notification.application.fcm.dto.FcmCreateRequestDto;
import com.example.momo.domain.notification.application.fcm.dto.FcmDeleteRequestDto;
import com.example.momo.domain.notification.application.fcm.dto.FcmMessageDto;
import com.example.momo.global.common.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fcm")
public class FcmController {

	private final FcmService fcmService;

	@PostMapping
	public ResponseEntity<ApiResponse<Void>> createToken(@RequestBody FcmCreateRequestDto dto,
		@AuthenticationPrincipal AuthUser authUser) {
		fcmService.createToken(authUser.getId(), dto);
		return ResponseEntity.ok(ApiResponse.success("저장완료", null));
	}

	@DeleteMapping
	public ResponseEntity<ApiResponse<Void>> deleteToken(@RequestBody FcmDeleteRequestDto dto,
		@AuthenticationPrincipal AuthUser authUser) {
		fcmService.deleteToken(authUser.getId(), dto.deviceId());
		return ResponseEntity.ok(ApiResponse.success("삭제완료", null));
	}

	@PostMapping("/send")
	public void send(@RequestBody FcmMessageDto dto) {
		fcmService.processFcmIfTokenExists(dto);
	}
}
