package com.example.momo.domain.messagehub.infra.redis;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.messagehub.application.dto.MeetingReminderMessage;
import com.example.momo.domain.messagehub.enums.AlarmType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class RedisReminderRepository {

	private final StringRedisTemplate redisTemplate;
	private final RedisTemplate<String, MeetingReminderMessage> redisReminderTemplate;

	private static final String ZSET_KEY = "reminder:meeting";
	private static final String HASH_KEY = "reminder:meeting:data";

	public void save(MeetingReminderMessage message, Instant notifyAt) {
		// 1. 고유 식별자 키 생성 (예: userId:meetingId)
		String uniqueKey = message.getUserId() + ":" + message.getMeetingId();

		// 2. ZSet에는 고유키를 score와 함께 저장
		redisTemplate.opsForZSet().add(ZSET_KEY, uniqueKey, (double)notifyAt.toEpochMilli());

		// 3. Hash에는 고유키로 전체 객체를 저장 (상세 데이터 관리)
		redisReminderTemplate.opsForHash().put(HASH_KEY, uniqueKey, message);
	}

	// Key 범위 조회
	public Set<String> findUniqueKeysByScoreRange(double minScore, double maxScore, int count) {
		return redisTemplate.opsForZSet()
			.rangeByScore(ZSET_KEY, minScore, maxScore, 0, count);
	}

	// 다건 조회
	public List<MeetingReminderMessage> findMessagesByKeys(Collection<String> uniqueKeys) {
		List<Object> objects = redisReminderTemplate.opsForHash()
			.multiGet(HASH_KEY, new ArrayList<>(uniqueKeys));
		return objects.stream()
			.filter(Objects::nonNull)
			.map(obj -> (MeetingReminderMessage)obj)
			.collect(Collectors.toList());
	}

	public boolean isSent(String today, String uniqueKey, AlarmType alarmType) {
		String sentKey = "reminder:sent:" + today;
		return Boolean.TRUE.equals(
			redisTemplate.opsForSet().isMember(sentKey, uniqueKey + ":" + alarmType.name())
		);
	}

	public void deleteSentMessage(String uniqueKey) {
		redisTemplate.opsForZSet().remove(ZSET_KEY, uniqueKey);
		redisReminderTemplate.opsForHash().delete(HASH_KEY, uniqueKey);
	}

	public void markAsSent(Collection<String> uniqueKeys, AlarmType alarmType, String today) {
		String[] members = uniqueKeys.stream()
			.map(key -> key + ":" + alarmType.name())
			.toArray(String[]::new);
		String sentKey = "reminder:sent:" + today;

		redisTemplate.opsForSet().add(sentKey, members);
		redisTemplate.expire(sentKey, Duration.ofDays(2));
	}
}


