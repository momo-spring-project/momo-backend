package com.example.momo.global.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Outbox 발행용 실행 스레드풀
 */
@Configuration
public class OutboxExecutorConfig {

	@Bean("outboxPublisherExecutor")
	public Executor outboxPublisherExecutor() {
		ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
		ex.setCorePoolSize(2);
		ex.setMaxPoolSize(4);
		ex.setQueueCapacity(50);
		ex.setThreadNamePrefix("outbox-");
		ex.initialize();
		return ex;
	}
}
