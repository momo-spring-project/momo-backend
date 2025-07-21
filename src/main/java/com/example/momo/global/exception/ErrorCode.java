package com.example.momo.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
	String getMessage(); // 에러 메시지

	HttpStatus getStatus(); // HTTP 상태코드
}
