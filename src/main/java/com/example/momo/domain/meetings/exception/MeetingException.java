package com.example.momo.domain.meetings.exception;

import com.example.momo.global.exception.BaseException;
import com.example.momo.global.exception.ErrorCode;

import lombok.Getter;

@Getter
public class MeetingException extends BaseException {

	public MeetingException(ErrorCode errorCode) {
		super(errorCode);
	}
}
