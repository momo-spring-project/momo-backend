package com.example.momo.domain.user.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class UserException extends RuntimeException {
	private final String errorCode;
	private final HttpStatus httpStatus;

	public static UserException userNotFound() {
		return new UserException("USER_NOT_FOUND", "유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
	}

	public static UserException invalidCategoryIds() {
		return new UserException("INVALID_CATEGORY_IDS", "유효하지 않은 카테고리 ID가 포함되어 있습니다.", HttpStatus.BAD_REQUEST);
	}

	public static UserException passwordMismatch() {
		return new UserException("PASSWORD_MISMATCH", "현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
	}

	public static UserException passwordConfirmMismatch() {
		return new UserException("PASSWORD_CONFIRM_MISMATCH", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
	}

	public static UserException duplicateNickname() {
		return new UserException("DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT);
	}

	public static UserException duplicateEmail() {
		return new UserException("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT);
	}

	private UserException(String errorCode, String message, HttpStatus httpStatus) {
		super(message);
		this.errorCode = errorCode;
		this.httpStatus = httpStatus;
	}
}
