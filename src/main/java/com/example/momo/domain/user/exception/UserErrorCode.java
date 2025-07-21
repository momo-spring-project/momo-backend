package com.example.momo.domain.user.exception;

import org.springframework.http.HttpStatus;

import com.example.momo.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
	// 사용자 관련 예외
	USER_NOT_FOUND("유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	TARGET_USER_NOT_FOUND("대상 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

	// 정보 수정 관련 예외
	PASSWORD_MISMATCH("현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
	PASSWORD_CONFIRM_MISMATCH("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
	DUPLICATE_NICKNAME("이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT),
	DUPLICATE_EMAIL("이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),

	// 카테고리 관련 예외
	INVALID_CATEGORY_IDS("유효하지 않은 카테고리 ID가 포함되어 있습니다.", HttpStatus.BAD_REQUEST),

	// 평가 관련 예외
	CANNOT_RATE_SELF("자기 자신을 평가할 수 없습니다.", HttpStatus.BAD_REQUEST),
	NOT_SAME_MEETING_PARTICIPANTS("같은 모임에 참가한 사용자만 평가할 수 있습니다.", HttpStatus.BAD_REQUEST),
	DUPLICATE_RATING("같은 모임에서 이미 해당 사용자를 평가했습니다.", HttpStatus.CONFLICT),

	// 팔로우 관련 예외
	CANNOT_FOLLOW_SELF("자기 자신을 팔로우할 수 없습니다.", HttpStatus.BAD_REQUEST),
	ALREADY_FOLLOWING("이미 팔로우한 사용자입니다.", HttpStatus.CONFLICT),
	NOT_FOLLOWING("팔로우하지 않은 사용자입니다.", HttpStatus.BAD_REQUEST);

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