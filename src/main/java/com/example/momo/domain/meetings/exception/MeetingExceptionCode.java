package com.example.momo.domain.meetings.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MeetingExceptionCode {
	// 400
	MEETING_IS_UNAVAILABLE(HttpStatus.BAD_REQUEST, "Meeting is unavailable"),

	// 403
	INSUFFICIENT_SCORE(HttpStatus.FORBIDDEN, "Insufficient score"),
	FAR_FROM_MEETING(HttpStatus.FORBIDDEN, "Far from meeting"),

	// 404
	MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, "Meeting not found"),
	PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "Participant not found"),

	// 409
	ALREADY_PARTICIPATED(HttpStatus.CONFLICT, "Already participated"),
	ALREADY_STARTED_MEETING(HttpStatus.CONFLICT, "Already started meeting"),
	ALREADY_FINISHED_MEETING(HttpStatus.CONFLICT, "Already finished meeting"),

	// 410
	DELETED_MEETING(HttpStatus.GONE, "Deleted meeting");

	private final HttpStatus httpStatus;
	private final String message;

	MeetingExceptionCode (HttpStatus httpStatus, String message) {
		this.httpStatus = httpStatus;
		this.message = message;
	}
}
