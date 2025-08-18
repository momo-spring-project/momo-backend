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
		ex.setCorePoolSize(4);
		ex.setMaxPoolSize(8);
		ex.setQueueCapacity(64); //대기열: 최악 32건(두 스케줄 겹침)의 2배
		ex.setThreadNamePrefix("outbox-");
		ex.setWaitForTasksToCompleteOnShutdown(true); // 종료 시 graceful shutdown
		ex.initialize();
		return ex;
	}
}
