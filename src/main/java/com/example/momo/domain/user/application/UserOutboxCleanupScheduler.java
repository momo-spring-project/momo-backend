package com.example.momo.domain.user.application;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.momo.domain.user.domain.UserOutboxEvent;

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

	private static final int MAX_RETRY_COUNT = 5;
	private static final int CLEANUP_DAYS_OLD = 7;

	/**
	 * 실패한 아웃박스 이벤트 재시도 (매 5분)
	 */
	@Scheduled(fixedRate = 300_000)
	public void retryFailedEvents() {
		try {
			log.info("실패한 아웃박스 이벤트 재시도 시작");

			List<UserOutboxEvent> retryableEvents =
				userOutboxService.getRetryableEvents(MAX_RETRY_COUNT);

			if (retryableEvents.isEmpty()) {
				log.debug("재시도할 아웃박스 이벤트가 없습니다");
				return;
			}

			log.info("재시도 대상 아웃박스 이벤트 {}개 발견", retryableEvents.size());

			int successCount = 0;
			int failCount = 0;

			for (UserOutboxEvent event : retryableEvents) {
				try {
					userOutboxService.retryEvent(event);
					successCount++;
				} catch (Exception e) {
					failCount++;
					log.warn("아웃박스 이벤트 재시도 실패: id={}, userId={}, retryCount={}",
						event.getId(), event.getUserId(), event.getRetryCount());
				}
			}

			log.info("아웃박스 이벤트 재시도 완료: 성공={}, 실패={}", successCount, failCount);

		} catch (Exception e) {
			log.error("아웃박스 이벤트 재시도 스케줄러 실행 중 오류 발생", e);
		}
	}

	/**
	 * 오래된 발행 완료 이벤트 정리 (매일 새벽 3시)
	 */
	@Scheduled(cron = "0 0 3 * * *")
	public void cleanupOldEvents() {
		try {
			log.info("오래된 아웃박스 이벤트 정리 시작: {}일 이전 데이터", CLEANUP_DAYS_OLD);

			int cleanedCount = userOutboxService.cleanupOldPublishedEvents(CLEANUP_DAYS_OLD);

			log.info("오래된 아웃박스 이벤트 정리 완료: {}개 삭제됨", cleanedCount);

		} catch (Exception e) {
			log.error("오래된 아웃박스 이벤트 정리 중 오류 발생", e);
		}
	}
}