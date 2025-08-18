package com.example.momo.global.webclient.user;

/**
 * 사용자 도메인 웹클라이언트 통신 시 발생하는 예외
 */
public class UserClientException extends RuntimeException {

	public UserClientException(String message) {
		super(message);
	}

	public UserClientException(String message, Throwable cause) {
		super(message, cause);
	}
}