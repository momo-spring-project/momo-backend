package com.example.momo.domain.messagehub.enums;

import java.time.Duration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 모임 알림 타입과 해당 알림의 기준 시간 간격을 정의하는 열거형.
 * DAY: 하루 전 알림, MIN30: 30분 전 알림.
 */
@Getter
@AllArgsConstructor
public enum AlarmType {
	DAY("MEETING_TOMORROW", Duration.ofDays(1)),
	MIN30("MEETING_UPCOMING", Duration.ofMinutes(30));

	private final String code;
	private final Duration duration;

}
