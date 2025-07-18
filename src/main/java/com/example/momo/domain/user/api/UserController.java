package com.example.momo.domain.user.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.momo.domain.common.dto.ApiResponse;
import com.example.momo.domain.user.application.UserService;
import com.example.momo.domain.user.domain.dto.UserInfoResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	// 특정 사용자 정보 조회
	@GetMapping("{userId}")
	public ResponseEntity<ApiResponse<UserInfoResponseDto>> getUserInfo(
		@PathVariable Long userId
	) {
		UserInfoResponseDto response = userService.getUserById(userId);
		return ResponseEntity.ok(ApiResponse.success("사용자 정보를 조회했습니다.", response));
	}

	// 현재 로그인안 사용자 정보 조회 (추후에 수정 예정)
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<UserInfoResponseDto>> getCurrentUser(
		@RequestHeader("user_id") Long currentUserId // TODO : 임시로 설정
	) {
		UserInfoResponseDto response = userService.getCurrentUser(currentUserId);
		return ResponseEntity.ok(ApiResponse.success("내 정보를 조회했습니다.", response));
	}
}
