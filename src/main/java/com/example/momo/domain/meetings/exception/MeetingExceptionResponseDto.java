package com.example.momo.domain.meetings.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeetingExceptionResponseDto {
	public String code;
	public String message;
}