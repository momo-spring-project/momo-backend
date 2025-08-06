package com.example.momo.domain.user.application;

import java.util.List;

import com.example.momo.domain.user.domain.UserOutboxEvent;

/**
 * 사용자 아웃박스 이벤트 서비스 인터페이스
 * 아웃박스 패턴 관련 비즈니스 로직 처리
 */
public interface UserOutboxService {

	void saveUserWithdrawnEvent(Long userId, String email, String nickname);

	void markEventAsPublished(Long userId, String eventType);

	List<UserOutboxEvent> getUnpublishedEvents();

	void publishEvent(UserOutboxEvent outboxEvent);

	int cleanupOldPublishedEvents(int daysOld);
}