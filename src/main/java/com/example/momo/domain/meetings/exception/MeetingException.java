package com.example.momo.domain.meetings.exception;

import lombok.Getter;

@Getter
public class MeetingException extends RuntimeException {

	private final MeetingExceptionCode exceptionCode;
	private final String exceptionMessage;

	public MeetingException(MeetingExceptionCode exceptionCode) {
		this.exceptionCode = exceptionCode;
		this.exceptionMessage = exceptionCode.getMessage();
	}
}
