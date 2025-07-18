package com.example.momo.domain.user.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.momo.domain.user")
public class UserExceptionHandler {

	@ExceptionHandler(UserException.class)
	public ResponseEntity<Map<String, Object>> handleProductException(UserException e) {
		Map<String, Object> response = new HashMap<>();
		response.put("error", e.getErrorCode());
		response.put("message", e.getMessage());
		response.put("status", e.getHttpStatus().value());

		return ResponseEntity.status(e.getHttpStatus()).body(response);
	}
}
