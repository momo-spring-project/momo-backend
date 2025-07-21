package com.example.momo.domain.meetings.exception;

import com.example.momo.global.exception.BaseException;
import lombok.Getter;

@Getter
public class MeetingException extends BaseException {

	private final MeetingExceptionCode exceptionCode;
	private final String exceptionMessage;

	public MeetingException(MeetingExceptionCode exceptionCode) {
		super(exceptionCode);
		this.exceptionCode = exceptionCode;
		this.exceptionMessage = exceptionCode.getMessage();
	}
}
