package com.example.momo.domain.payments.exception;

import com.example.momo.global.exception.BaseException;
import com.example.momo.global.exception.ErrorCode;

public class PaymentException extends BaseException {

  public PaymentException(ErrorCode errorCode) {
    super(errorCode);
  }
}
