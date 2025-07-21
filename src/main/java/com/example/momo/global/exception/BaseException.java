package com.example.momo.global.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
	private final ErrorCode errorCode;

	public BaseException(ErrorCode errorCode) {
		super(errorCode.getMessage()); // 부모 클래스에 메시지 전달
		this.errorCode = errorCode;
	}
}
