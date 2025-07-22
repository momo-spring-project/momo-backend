package com.example.momo.domain.payments.exception;

import com.example.momo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

  // 4xx
  ALREADY_PAID("이미 결제가 완료된 사용자입니다.", HttpStatus.CONFLICT),
  MEETING_NOT_FOUND("모임을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  PAYMENT_NOT_FOUND("결제 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  TEST_KEY_ONLY("Key-in API는 테스트 키에서만 호출할 수 있습니다.", HttpStatus.FORBIDDEN),
  REFUND_NOT_ALLOWED("완료된 결제만 환불 가능합니다.", HttpStatus.BAD_REQUEST),

  // 5xx
  TOSS_CONFIRM_FAILED("결제 승인 실패", HttpStatus.BAD_GATEWAY),
  REFUND_FAILED("환불 처리 실패", HttpStatus.BAD_GATEWAY),
  UNSUPPORTED_PAYMENT_STATUS("지원하지 않는 결제 상태입니다.", HttpStatus.INTERNAL_SERVER_ERROR);

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