package com.example.momo.domain.user.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.momo.domain.common.dto.ApiResponse;

@RestControllerAdvice(basePackages = "com.example.momo.domain.user")
public class UserExceptionHandler {

	@ExceptionHandler(UserException.class)
	public ResponseEntity<ApiResponse<Object>> handleUserException(UserException e) {
		ApiResponse<Object> response = ApiResponse.fail(e.getMessage(), null);
		return ResponseEntity.status(e.getHttpStatus()).body(response);
	}

	/**
	 * DB 제약조건 위반 (외래키 제약조건 등)
	 * 카테고리 ID, 모임 ID, 사용자 ID 등 다양한 경우에 발생 가능
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
		ApiResponse<Object> response = ApiResponse.fail("잘못된 요청입니다. 유효하지 않은 데이터가 포함되어 있습니다.", null);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	/**
	 * 기타 예외
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
		ApiResponse<Object> response = ApiResponse.fail("서버 오류가 발생했습니다.", null);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
