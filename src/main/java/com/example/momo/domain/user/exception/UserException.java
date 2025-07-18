package com.example.momo.domain.user.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class UserException extends RuntimeException {
	private final String errorCode;
	private final HttpStatus httpStatus;

	// === 사용자 관련 예외 ===

	public static UserException userNotFound() {
		return new UserException("USER_NOT_FOUND", "유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
	}

	public static UserException targetUserNotFound() {
		return new UserException("TARGET_USER_NOT_FOUND", "대상 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
	}

	// === 정보 수정 관련 예외 ===

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

	// === 카테고리 관련 예외 ===

	public static UserException invalidCategoryIds() {
		return new UserException("INVALID_CATEGORY_IDS", "유효하지 않은 카테고리 ID가 포함되어 있습니다.", HttpStatus.BAD_REQUEST);
	}

	// === 평가 관련 예외 ===

	public static UserException cannotRateSelf() {
		return new UserException("CANNOT_RATE_SELF", "자기 자신을 평가할 수 없습니다.", HttpStatus.BAD_REQUEST);
	}

	public static UserException notSameMeetingParticipants() {
		return new UserException("NOT_SAME_MEETING_PARTICIPANTS", "같은 모임에 참가한 사용자만 평가할 수 있습니다.",
			HttpStatus.BAD_REQUEST);
	}

	public static UserException duplicateRating() {
		return new UserException("DUPLICATE_RATING", "같은 모임에서 이미 해당 사용자를 평가했습니다.", HttpStatus.CONFLICT);
	}

	// === 팔로우 관련 예외 ===

	public static UserException cannotFollowSelf() {
		return new UserException("CANNOT_FOLLOW_SELF", "자기 자신을 팔로우할 수 없습니다.", HttpStatus.BAD_REQUEST);
	}

	public static UserException alreadyFollowing() {
		return new UserException("ALREADY_FOLLOWING", "이미 팔로우한 사용자입니다.", HttpStatus.CONFLICT);
	}

	public static UserException notFollowing() {
		return new UserException("NOT_FOLLOWING", "팔로우하지 않은 사용자입니다.", HttpStatus.BAD_REQUEST);
	}

	private UserException(String errorCode, String message, HttpStatus httpStatus) {
		super(message);
		this.errorCode = errorCode;
		this.httpStatus = httpStatus;
	}
}
