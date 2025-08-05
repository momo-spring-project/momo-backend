package com.example.momo.domain.user.application;

import java.util.List;

import com.example.momo.domain.user.domain.UserOutboxEvent;

/**
 * 사용자 아웃박스 이벤트 서비스 인터페이스
 * 아웃박스 패턴 관련 비즈니스 로직 처리
 */
public interface UserOutboxService {

	/**
	 * 회원탈퇴 아웃박스 이벤트 저장
	 */
	void saveUserWithdrawnEvent(Long userId, String email, String nickname);

	/**
	 * 아웃박스 이벤트를 발행 완료로 마킹
	 */
	void markEventAsPublished(Long userId, String eventType);

	/**
	 * 재시도 가능한 미발행 이벤트 목록 조회
	 */
	List<UserOutboxEvent> getRetryableEvents(int maxRetryCount);

	/**
	 * 이벤트 재시도 처리
	 */
	void retryEvent(UserOutboxEvent outboxEvent);

	/**
	 * 발행 완료된 오래된 이벤트 정리
	 */
	int cleanupOldPublishedEvents(int daysOld);
}