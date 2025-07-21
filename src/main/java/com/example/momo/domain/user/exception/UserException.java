package com.example.momo.domain.user.exception;

import com.example.momo.global.exception.BaseException;
import com.example.momo.global.exception.ErrorCode;

public class UserException extends BaseException {

	public UserException(ErrorCode errorCode) {
		super(errorCode);
	}
}