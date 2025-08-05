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
	 *
	 * @param userId 사용자 ID
	 * @param email 사용자 이메일
	 * @param nickname 사용자 닉네임
	 */
	void saveUserWithdrawnEvent(Long userId, String email, String nickname);

	/**
	 * 아웃박스 이벤트를 발행 완료로 마킹
	 *
	 * @param userId 사용자 ID
	 * @param eventType 이벤트 타입
	 */
	void markEventAsPublished(Long userId, String eventType);

	/**
	 * 미발행된 이벤트 목록 조회
	 *
	 * @return 미발행 이벤트 목록
	 */
	List<UserOutboxEvent> getUnpublishedEvents();

	/**
	 * 재시도 가능한 미발행 이벤트 목록 조회
	 *
	 * @param maxRetryCount 최대 재시도 횟수
	 * @return 재시도 가능한 미발행 이벤트 목록
	 */
	List<UserOutboxEvent> getRetryableEvents(int maxRetryCount);

	/**
	 * 이벤트 재시도 처리
	 *
	 * @param outboxEvent 재시도할 아웃박스 이벤트
	 */
	void retryEvent(UserOutboxEvent outboxEvent);

	/**
	 * 발행 완료된 오래된 이벤트 정리
	 *
	 * @param daysOld 며칠 이전 데이터를 정리할지
	 * @return 정리된 이벤트 개수
	 */
	int cleanupOldPublishedEvents(int daysOld);
}