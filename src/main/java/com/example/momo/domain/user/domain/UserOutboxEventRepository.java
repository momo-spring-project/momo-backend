package com.example.momo.domain.user.domain;

import java.util.List;

/**
 * 사용자 아웃박스 이벤트 저장소 인터페이스
 */
public interface UserOutboxEventRepository {

	/**
	 * 아웃박스 이벤트 저장
	 */
	void save(UserOutboxEvent outboxEvent);

	/**
	 * 특정 사용자의 특정 이벤트 타입을 발행 완료로 마킹
	 */
	void markAsPublished(Long userId, String eventType);

	/**
	 * 미발행된 이벤트 목록 조회 (재시도용)
	 */
	List<UserOutboxEvent> findUnpublishedEvents();

	/**
	 * 특정 재시도 횟수 이하인 미발행 이벤트 목록 조회
	 */
	List<UserOutboxEvent> findUnpublishedEventsWithRetryCountLessThan(int maxRetryCount);

	/**
	 * 재시도 횟수 증가
	 */
	void incrementRetryCount(Long outboxEventId);

	/**
	 * 발행 완료된 오래된 이벤트 삭제
	 *
	 * @param daysOld 며칠 이전 데이터를 삭제할지
	 * @return 삭제된 이벤트 개수
	 */
	int deleteOldPublishedEvents(int daysOld);
}