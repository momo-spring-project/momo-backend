package com.example.momo.domain.meetings.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MeetingExceptionHandler {

	@ExceptionHandler(MeetingException.class)
	public ResponseEntity<MeetingExceptionResponseDto> handleMeetingException(MeetingException e) {
		MeetingExceptionCode exceptionCode = e.getExceptionCode();
		MeetingExceptionResponseDto responseDto =
			new MeetingExceptionResponseDto(exceptionCode.name(), e.getMessage());
		return new ResponseEntity<>(responseDto, exceptionCode.getHttpStatus());
	}
}