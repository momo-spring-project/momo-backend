package com.example.momo.domain.messagehub.application.util;

import com.example.momo.domain.messagehub.application.dto.MeetingReminderMessage;
import com.example.momo.domain.messagehub.enums.AlarmType;

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

}
