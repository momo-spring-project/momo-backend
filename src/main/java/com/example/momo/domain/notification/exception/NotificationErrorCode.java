package com.example.momo.domain.notification.exception;

import org.springframework.http.HttpStatus;

import com.example.momo.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {
	NOT_FOUND_RECEIVER("알림 수신 대상이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
	INVALID_NOTIFICATION_REQUEST("잘못된 알림 요청입니다.", HttpStatus.BAD_REQUEST),
	UNSUPPORTED_NOTIFICATION_TYPE("지원하지 않는 알림 유형입니다.", HttpStatus.BAD_REQUEST),
	NOTIFICATION_DELIVERY_FAILED("알림 전송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

	private final String message;
	private final HttpStatus status;

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public HttpStatus getStatus() {
		return status;
	}
}
