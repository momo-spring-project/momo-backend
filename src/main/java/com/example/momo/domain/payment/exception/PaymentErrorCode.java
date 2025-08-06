package com.example.momo.domain.payment.exception;

import org.springframework.http.HttpStatus;

import com.example.momo.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

	// 4xx
	ALREADY_PAID("이미 결제가 완료된 사용자입니다.", HttpStatus.CONFLICT),
	MEETING_NOT_FOUND("모임을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	PAYMENT_NOT_FOUND("결제 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	MEETING_ALREADY_STARTED("이미 시작된 모임은 환불할 수 없습니다.", HttpStatus.BAD_REQUEST),
	TEST_KEY_ONLY("Key-in API는 테스트 키에서만 호출할 수 있습니다.", HttpStatus.FORBIDDEN),
	REFUND_NOT_ALLOWED("완료된 결제만 환불 가능합니다.", HttpStatus.BAD_REQUEST),

	UNAUTHORIZED_PAYMENT("결제 권한이 없습니다.", HttpStatus.FORBIDDEN),
	UNAUTHORIZED_REFUND("환불 권한이 없습니다.", HttpStatus.FORBIDDEN),

	INVALID_PAYMENT_STATUS("결제 상태가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
	RETRY_TIME_EXPIRED("재시도 가능 시간이 만료되었습니다.", HttpStatus.BAD_REQUEST),
	FREE_MEETING_PARTICIPATION_FAILED("무료 모임 참가 처리에 실패했습니다", HttpStatus.BAD_REQUEST),

	// 5xx
	TOSS_CONFIRM_FAILED("결제 승인 실패", HttpStatus.BAD_GATEWAY),
	REFUND_FAILED("환불 처리 실패", HttpStatus.BAD_GATEWAY);

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