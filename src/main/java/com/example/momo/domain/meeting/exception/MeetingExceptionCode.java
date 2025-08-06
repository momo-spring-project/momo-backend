package com.example.momo.domain.meeting.exception;

import org.springframework.http.HttpStatus;

import com.example.momo.global.exception.ErrorCode;

public enum MeetingExceptionCode implements ErrorCode {
	// 400
	MEETING_IS_UNAVAILABLE(HttpStatus.BAD_REQUEST, "Meeting is unavailable"),

	// 403
	INSUFFICIENT_SCORE(HttpStatus.FORBIDDEN, "Insufficient score"),
	FAR_FROM_MEETING(HttpStatus.FORBIDDEN, "Far from meeting"),
	MEETING_FORBIDDEN(HttpStatus.FORBIDDEN, "Meeting is forbidden"),
	MEETING_TIME_FORBIDDEN(HttpStatus.FORBIDDEN, "Meeting time forbidden"),

	// 404
	MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "Meeting not found"),
	PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "Participant not found"),

	// 409
	ALREADY_PARTICIPATED(HttpStatus.CONFLICT, "Already participated"),
	ALREADY_STARTED_MEETING(HttpStatus.CONFLICT, "Already started meeting"),
	ALREADY_FINISHED_MEETING(HttpStatus.CONFLICT, "Already finished meeting"),
	MEETING_IS_FULL(HttpStatus.CONFLICT, "Meeting is full"),
	INVALID_PARTICIPANT_COUNT(HttpStatus.CONFLICT, "Invalid participant count"),

	// 410
	DELETED_MEETING(HttpStatus.GONE, "Deleted meeting");

	private final HttpStatus status;
	private final String message;

	MeetingExceptionCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public HttpStatus getStatus() {
		return this.status;
	}
}