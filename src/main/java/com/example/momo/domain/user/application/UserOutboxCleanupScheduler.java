package com.example.momo.domain.user.application;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.momo.domain.user.domain.UserOutboxEvent;
import com.example.momo.domain.user.domain.UserOutboxService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 아웃박스 이벤트 정리 및 재시도 스케줄러
 * 실패한 이벤트 재시도 및 오래된 성공 이벤트 정리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserOutboxCleanupScheduler {

	private final UserOutboxService userOutboxService;

	private static final int CLEANUP_DAYS_OLD = 7;

	@Scheduled(fixedRate = 300_000)
	public void retryUnpublishedEvents() {
		List<UserOutboxEvent> events = userOutboxService.getUnpublishedEvents();
		for (UserOutboxEvent event : events) {
			try {
				userOutboxService.publishEvent(event);
			} catch (Exception e) {
				log.warn("발행 실패: id={}, userId={}", event.getId(), event.getUserId());
			}
		}
	}

	@Scheduled(cron = "0 0 3 * * *")
	public void cleanupOldEvents() {
		int deleted = userOutboxService.cleanupOldPublishedEvents(CLEANUP_DAYS_OLD);
		log.info("오래된 이벤트 {}개 삭제됨", deleted);
	}
}