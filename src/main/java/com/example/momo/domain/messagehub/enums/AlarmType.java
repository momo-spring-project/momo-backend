package com.example.momo.domain.messagehub.enums;

import java.time.Duration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AlarmType {
	DAY("MEETING_TOMORROW", Duration.ofDays(1)),
	MIN30("MEETING_UPCOMING", Duration.ofMinutes(30));

	private final String code;
	private final Duration duration;

}
