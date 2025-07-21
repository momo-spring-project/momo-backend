package com.example.momo.domain.notification.entity.exception;

import com.example.momo.global.exception.BaseException;
import com.example.momo.global.exception.ErrorCode;

public class NotificationException extends BaseException {
	public NotificationException(ErrorCode errorCode) {
		super(errorCode);
	}
}
