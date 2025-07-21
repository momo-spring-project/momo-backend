package com.example.momo.domain.meetings.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class MeetingExceptionHandler {

	@ExceptionHandler(MeetingException.class)
	public ResponseEntity<MeetingExceptionResponseDto> handleMeetingException(MeetingException e) {
		MeetingExceptionCode exceptionCode = e.getExceptionCode();
		MeetingExceptionResponseDto responseDto =
			new MeetingExceptionResponseDto(exceptionCode.name(), e.getMessage());
		log.info("Exception caught in MeetingExceptionHandler");
		return new ResponseEntity<>(responseDto, exceptionCode.getStatus());
	}
}