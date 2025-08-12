package com.example.momo.global.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.momo.domain.meeting.application.dto.response.MeetingResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableCaching
public class RedisCacheConfig {

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory cf, ObjectMapper objectMapper) {
		new Jackson2JsonRedisSerializer<>(MeetingResponseDto.class);

		// 공통 기본 설정 (키=String, 값=JSON, 기본 TTL=5분)
		RedisCacheConfiguration defaultCfg = RedisCacheConfiguration.defaultCacheConfig()
		// 키는 항상 String
		.serializeKeysWith(
			RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
		// value는 JSON (타입 정보도 포함됨)
		.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)))
		// "cacheName::key" 형태(prefix 추가)
		.computePrefixWith(CacheKeyPrefix.simple())
		// null 결과는 캐시하지 않음
		.disableCachingNullValues()
		// 기본 TTL
		.entryTtl(Duration.ofMinutes(3));

		// meeting 전용 직렬화기
		Jackson2JsonRedisSerializer<MeetingResponseDto> meetingSerializer = new Jackson2JsonRedisSerializer<>(objectMapper,MeetingResponseDto.class);


		// 캐시별 오버라이드
		RedisCacheConfiguration meetingCfg = defaultCfg
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(meetingSerializer))
			.entryTtl(Duration.ofMinutes(5));

		RedisCacheConfiguration userCfg    = defaultCfg.entryTtl(Duration.ofMinutes(10));

		return RedisCacheManager.builder(cf)
			.cacheDefaults(defaultCfg)                  // 기본: 5분
			.withCacheConfiguration("meeting", meetingCfg) // meeting 전용
			// .withCacheConfiguration("user", userCfg)
			.build();
	}
}
