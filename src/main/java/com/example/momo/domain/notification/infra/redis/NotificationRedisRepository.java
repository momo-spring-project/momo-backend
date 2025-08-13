package com.example.momo.domain.notification.infra.redis;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRedisRepository {
	private final RedisTemplate<String, String> redisStringTemplate;

	public boolean isUuidYesterdayKeyExist(String uuid, String yesterdayKey) {
		return Boolean.TRUE.equals(redisStringTemplate.opsForSet().isMember(yesterdayKey, uuid));
	}

	public Long saveUuidKeyWithTodayKey(String uuid, String todayKey) {

		Long savedUuid = redisStringTemplate.opsForSet().add(todayKey, uuid);

		redisStringTemplate.expire(todayKey, Duration.ofDays(2));

		return savedUuid;
	}
}
