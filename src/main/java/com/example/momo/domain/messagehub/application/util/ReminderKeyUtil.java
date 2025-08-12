package com.example.momo.domain.messagehub.application.util;

import com.example.momo.domain.messagehub.application.dto.MeetingReminderMessage;
import com.example.momo.domain.messagehub.enums.AlarmType;

/**
 * 모임 알림 관련 Redis 키를 생성·관리하는 유틸리티 클래스.
 * ZSET/HASH 저장 키, 발송 마킹 키 및 멤버를 규칙에 맞게 변환하는 기능을 제공.
 */
public class ReminderKeyUtil {

	public static final String ZSET_KEY = "reminder:meeting";
	public static final String HASH_KEY = "reminder:meeting:data";

	public static String toUniqueKey(MeetingReminderMessage message) {
		return message.getUserId() + ":" + message.getMeetingId();
	}

	public static String toSentKeyWithToday(String today) {
		return "reminder:sent:" + today;
	}

	public static String toSentMark(String uniqueKey, AlarmType alarmType) {
		return uniqueKey + ":" + alarmType.name();
	}

	public static String toUuidMarkKey(String today) {
		return "uuid:mark:" + today;
	}

}
