package com.example.momo.domain.messagehub.infra.redis;

import static com.example.momo.domain.messagehub.application.util.ReminderKeyUtil.*;

import java.time.Duration;
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
import com.example.momo.domain.messagehub.application.dto.ScoreRangeDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class RedisReminderRepository {

	private final StringRedisTemplate redisTemplate;
	private final RedisTemplate<String, MeetingReminderMessage> redisReminderTemplate;

	public void saveZsetMessage(String uniqueKey, long meetingTime) {
		// ZSet 에는 고유키를 score 와 함께 저장
		redisTemplate.opsForZSet().add(ZSET_KEY, uniqueKey, meetingTime);
	}

	public void saveHashMessage(String uniqueKey, MeetingReminderMessage message) {
		// Hash 에는 고유키로 전체 객체를 저장 (상세 데이터 관리)
		redisReminderTemplate.opsForHash().put(HASH_KEY, uniqueKey, message);
	}

	// Key 범위 조회
	public Set<String> findUniqueKeysByScoreRange(ScoreRangeDto dto) {
		return redisTemplate.opsForZSet()
			.rangeByScore(ZSET_KEY, dto.fromScore(), dto.toScore(), 0, dto.maxCount());
	}

	// 다건 조회
	public List<MeetingReminderMessage> findMessagesByKeys(Collection<String> uniqueKeys) {
		List<Object> objects = redisReminderTemplate.opsForHash()
			.multiGet(HASH_KEY, new ArrayList<>(uniqueKeys));

		return objects.stream()
			.map(MeetingReminderMessage::of)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	public boolean isSent(String sentKey, String sentMark) {

		return Boolean.TRUE.equals(
			redisTemplate.opsForSet().isMember(sentKey, sentMark)
		);
	}

	public void deleteSentMessage(String uniqueKey) {
		redisTemplate.opsForZSet().remove(ZSET_KEY, uniqueKey);
		redisReminderTemplate.opsForHash().delete(HASH_KEY, uniqueKey);
	}

	public void deleteSentMessages(Set<String> keys) {
		redisTemplate.opsForZSet().remove(ZSET_KEY, keys.toArray());
		redisReminderTemplate.opsForHash().delete(HASH_KEY, keys.toArray());
	}

	public void markAsSent(String sentKey, String[] members) {

		redisTemplate.opsForSet().add(sentKey, members);
		redisTemplate.expire(sentKey, Duration.ofDays(2));
	}
}


