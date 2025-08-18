package com.example.momo.domain.messagehub.infra.redis;

import static com.example.momo.domain.messagehub.application.util.ReminderKeyUtil.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.messagehub.application.dto.MeetingReminderMessage;
import com.example.momo.domain.messagehub.application.dto.ScoreRangeDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * 모임 알림 예약 데이터를 Redis에 저장·조회·삭제·마킹하는 저장소.
 * ZSET을 시간 기반 인덱스로, HASH를 상세 데이터 저장소로 활용하며,
 * 발송 마킹 여부 확인 및 잔여 데이터 정리 기능을 제공.
 */
@RequiredArgsConstructor
@Repository
public class MessageHubRedisRepository {

	private final RedisTemplate<String, String> redisStringTemplate;
	private final RedisTemplate<String, MeetingReminderMessage> redisReminderTemplate;
	private final DefaultRedisScript<Void> saveReminderScript;
	private final DefaultRedisScript<Void> deleteReminderScript;
	private final ObjectMapper objectMapper;

	public void saveMessage(String uniqueKey, long meetingTime, MeetingReminderMessage message) throws
		Exception {

		// json 매핑
		String json = objectMapper.writeValueAsString(message);
		// 커넥션 바인딩 시작
		redisStringTemplate.execute(
			saveReminderScript,
			Arrays.asList(ZSET_KEY, HASH_KEY),
			String.valueOf(meetingTime),
			uniqueKey,
			json
		);
	}

	// Score 범위로 Key Set 조회
	public Set<String> findUniqueKeysByScoreRange(ScoreRangeDto dto) {
		return redisStringTemplate.opsForZSet()
			.rangeByScore(ZSET_KEY, dto.fromScore(), dto.toScore(), 0, dto.maxCount());
	}

	//Key Set 으로 객체 리스트 조회
	public List<MeetingReminderMessage> findMessagesByKeys(Collection<String> uniqueKeys) {
		List<Object> objects = redisReminderTemplate.opsForHash()
			.multiGet(HASH_KEY, new ArrayList<>(uniqueKeys));

		return objects.stream()
			.map(MeetingReminderMessage::of)
			.filter(Objects::nonNull)
			.toList();
	}

	//발송 된(하루전알린) 알림으로 마킹(저장)
	public void markAsSent(String sentKey, String[] members) {

		redisStringTemplate.opsForSet().add(sentKey, members);
		redisStringTemplate.expire(sentKey, Duration.ofDays(2));
	}

	//이미 발송된(하루전알림) 알림인지 확인
	public boolean isSent(String sentKey, String sentMark) {

		return Boolean.TRUE.equals(
			redisStringTemplate.opsForSet().isMember(sentKey, sentMark)
		);
	}

	// 단건 삭제
	public void deleteSentMessage(String uniqueKey) {
		redisStringTemplate.execute(
			deleteReminderScript,
			Arrays.asList(ZSET_KEY, HASH_KEY),
			uniqueKey
		);
	}

	// 다건 삭제
	public void deleteSentMessages(Set<String> keys) {
		redisStringTemplate.execute(
			deleteReminderScript,
			Arrays.asList(ZSET_KEY, HASH_KEY),
			keys.toArray()
		);
	}

	public boolean isUuidExist(String uuid, String todayKey, String yesterdayKey) {
		return Boolean.TRUE.equals(redisStringTemplate.opsForSet().isMember(todayKey, uuid)) || Boolean.TRUE.equals(
			redisStringTemplate.opsForSet().isMember(yesterdayKey, uuid));
	}

	public void saveUuidKeyWithTodayKey(String uuid, String todayKey) {

		redisStringTemplate.opsForSet().add(todayKey, uuid);

		redisStringTemplate.expire(todayKey, Duration.ofDays(2));
	}
}


