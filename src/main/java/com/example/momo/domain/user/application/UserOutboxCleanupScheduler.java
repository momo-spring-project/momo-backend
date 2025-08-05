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
	 * 실패한 아웃박스 이벤트 재시도
	 * 매 5분마다 실행
	 */
	@Scheduled(fixedRate = 300_000) // 5분 = 300,000ms
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
	 * 오래된 발행 완료 이벤트 정리
	 * 매일 새벽 3시에 실행
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

	/**
	 * 아웃박스 상태 모니터링
	 * 매 30분마다 실행
	 */
	@Scheduled(fixedRate = 1_800_000) // 30분 = 1,800,000ms
	public void monitorOutboxStatus() {
		try {
			List<UserOutboxEvent> unpublishedEvents = userOutboxService.getUnpublishedEvents();

			if (!unpublishedEvents.isEmpty()) {
				log.warn("미발행 아웃박스 이벤트 {}개 발견 - 확인 필요", unpublishedEvents.size());

				// 재시도 횟수 초과한 이벤트 체크
				long maxRetryExceeded = unpublishedEvents.stream()
					.filter(event -> event.getRetryCount() >= MAX_RETRY_COUNT)
					.count();

				if (maxRetryExceeded > 0) {
					log.error("재시도 횟수 초과한 아웃박스 이벤트 {}개 발견 - 수동 처리 필요",
						maxRetryExceeded);
				}
			}

		} catch (Exception e) {
			log.error("아웃박스 상태 모니터링 중 오류 발생", e);
		}
	}
}