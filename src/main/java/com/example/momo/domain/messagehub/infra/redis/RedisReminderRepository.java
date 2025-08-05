package com.example.momo.domain.messagehub.infra.redis;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.messagehub.application.dto.MeetingReminderMessage;
import com.example.momo.domain.messagehub.enums.AlarmType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class RedisReminderRepository {

	private final RedisTemplate<String, MeetingReminderMessage> redisTemplate;

	private static final String ZSET_KEY = "reminder:meeting";

	public void save(MeetingReminderMessage message, Instant notifyAt) {
		redisTemplate.opsForZSet().add(ZSET_KEY, message, (double)notifyAt.toEpochMilli());
	}

	public Set<MeetingReminderMessage> findByScoreRange(
		double minScore, double maxScore, int count) {
		return redisTemplate.opsForZSet()
			.rangeByScore(ZSET_KEY, minScore, maxScore, 0, count);
	}

	public void deleteSentMessages(Set<MeetingReminderMessage> messages) {
		messages.forEach(msg -> redisTemplate.opsForZSet().remove(ZSET_KEY, msg));
	}

	public boolean isAlreadySent(MeetingReminderMessage msg, AlarmType type) {
		String key = buildSentKey(msg, type);
		// 값 자체를 꺼내지 않고, 키가 있으면 이미 발송된 것으로 간주
		return redisTemplate.hasKey(key);
	}

	public void markAsSent(MeetingReminderMessage msg, AlarmType type) {
		String key = buildSentKey(msg, type);
		redisTemplate.opsForValue().set(key, msg, 2, TimeUnit.DAYS); // TTL 2일
	}

	private String buildSentKey(MeetingReminderMessage msg, AlarmType type) {
		return String.format("reminder:sent:%d:%d:%s", msg.getUserId(), msg.getMeetingId(), type.name());
	}
}


