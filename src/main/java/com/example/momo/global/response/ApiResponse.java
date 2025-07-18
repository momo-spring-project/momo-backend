package com.example.momo.global.response;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
	private boolean success;         // 성공/실패 여부
	private String message;          // 사용자에게 보여줄 메시지
	private T data;                  // 실제 데이터
	private LocalDateTime timestamp; // 응답 시간

	private ApiResponse(boolean success, String message, T data) {
		this.success = success;
		this.message = message;
		this.data = data;
		this.timestamp = LocalDateTime.now();
	}

	// 정적 팩토리 메서드로 객체 생성
	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(true, message, data);
	}

	public static <T> ApiResponse<T> error(String message) {
		return new ApiResponse<>(false, message, null);
	}
}

