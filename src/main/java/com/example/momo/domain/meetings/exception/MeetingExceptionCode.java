package com.example.momo.domain.meetings.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum MeetingExceptionCode {
	// 400
	MEETING_IS_UNAVAILABLE(HttpStatus.BAD_REQUEST, "Meeting is unavailable"),

	// 403
	INSUFFICIENT_SCORE(HttpStatus.FORBIDDEN, "Insufficient score"),
	MEETING_FORBIDDEN(HttpStatus.FORBIDDEN, "Meeting is forbidden"),
	// 404
	MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "Meeting not found"),
	PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "Participant not found"),

	// 409
	ALREADY_PARTICIPATED(HttpStatus.CONFLICT, "Already participated");

	private final HttpStatus httpStatus;
	private final String message;

	MeetingExceptionCode(HttpStatus httpStatus, String message) {
		this.httpStatus = httpStatus;
		this.message = message;
	}
}
