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

	public boolean isSent(String uniqueKey, AlarmType alarmType) {
		String sentKey = buildSentKey(uniqueKey, alarmType);
		return Boolean.TRUE.equals(redisTemplate.hasKey(sentKey));
	}

	public void deleteSentMessages(Set<String> uniqueKeys) {
		redisReminderTemplate.opsForZSet().remove(ZSET_KEY, uniqueKeys.toArray());
		redisReminderTemplate.opsForHash().delete(HASH_KEY, uniqueKeys.toArray());
	}

	public void markAsSent(Collection<String> uniqueKeys, AlarmType alarmType) {
		for (String key : uniqueKeys) {
			// type까지 포함한 sentKey를 생성
			String sentKey = buildSentKey(key, alarmType);
			// 발송 완료 마킹 (값은 단순히 1, "sent", 타임스탬프 등 자유롭게)
			redisTemplate.opsForValue().set(sentKey, "1");
			// 필요시 만료시간도 같이 줄 수 있음
			redisTemplate.expire(sentKey, Duration.ofDays(2));
		}
	}

	private String buildSentKey(String uniqueKey, AlarmType type) {
		// uniqueKey가 "userId:meetingId"라고 가정
		return String.format("reminder:sent:%s:%s", uniqueKey, type.name());
	}
}


