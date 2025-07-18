package com.example.momo.domain.meetings.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MeetingExceptionCode {
	//404
	MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "Meeting not found"),;

	private final HttpStatus httpStatus;
	private final String message;

	MeetingExceptionCode (HttpStatus httpStatus, String message) {
		this.httpStatus = httpStatus;
		this.message = message;
	}
}
