package com.example.momo.global.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

	@Value("${REDIS_HOST}")
	private String redisHost;

	@Value("${REDIS_PORT}")
	private int redisPort;

	@Bean(destroyMethod = "shutdown")
	public RedissonClient redissonClient() {
		Config config = new Config();

		SingleServerConfig singleServerConfig = config.useSingleServer()
			.setAddress("redis://" + redisHost + ":" + redisPort) // 반드시 redis:// 또는 rediss://
			.setDatabase(0)
			.setConnectionPoolSize(64)            // 최대 pool 커넥션 수
			.setConnectionMinimumIdleSize(24)    // 최소 idle 커넥션 수
			.setIdleConnectionTimeout(10000)    // 커넥션 idle 시간(10초) 이후 닫기
			.setConnectTimeout(10000)            // 커넥션 생성 시도 타임아웃(10초)
			.setTimeout(3000);                    // Redis 명령어 응답 대기 시간(3초)

		System.out.println("singleServerConfig.getAddress = " + singleServerConfig.getAddress());

		// 재시도 설정
		singleServerConfig
			.setRetryAttempts(3)        // 최대 재시도 횟수
			.setRetryInterval(1500);    // 재시도 간격(1.5초)

		// 락 Watchdog 설정 (Deadlock 방지용)
		config.setLockWatchdogTimeout(30000); // 30초

		// 기본 직렬화 Codec 설정 (선택)
		config.setCodec(new org.redisson.codec.JsonJacksonCodec());

		return Redisson.create(config);
	}
}
