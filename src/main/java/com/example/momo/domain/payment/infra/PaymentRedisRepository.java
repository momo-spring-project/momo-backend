package com.example.momo.domain.payment.infra;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PaymentRedisRepository {

	private final RedisTemplate<String, String> redisStringTemplate;

	public boolean isUuidExist(String uuid, String todayKey, String yesterdayKey) {
		return Boolean.TRUE.equals(redisStringTemplate.opsForSet().isMember(todayKey, uuid)) || Boolean.TRUE.equals(
			redisStringTemplate.opsForSet().isMember(yesterdayKey, uuid));
	}

	public void saveUuidKeyWithTodayKey(String uuid, String todayKey) {
		redisStringTemplate.opsForSet().add(todayKey, uuid);

		redisStringTemplate.expire(todayKey, Duration.ofDays(2));
	}
}
