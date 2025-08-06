package com.example.momo.domain.user.infra;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.momo.domain.user.domain.UserOutboxEvent;
import com.example.momo.domain.user.domain.UserOutboxEventRepository;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 아웃박스 이벤트 저장소 구현체
 */
@Repository
@RequiredArgsConstructor
public class UserOutboxEventRepositoryImpl implements UserOutboxEventRepository {

	private final UserOutboxJpaRepository userOutboxJpaRepository;
	private final UserOutboxQueryRepository userOutboxQueryRepository;

	@Override
	public void save(UserOutboxEvent outboxEvent) {
		userOutboxJpaRepository.save(outboxEvent);
	}

	@Override
	public void markAsPublished(Long userId, String eventType) {
		userOutboxQueryRepository.markAsPublished(userId, eventType);
	}

	@Override
	public List<UserOutboxEvent> findUnpublishedEvents() {
		return userOutboxQueryRepository.findUnpublishedEvents();
	}

	@Override
	public int deleteOldPublishedEvents(int daysOld) {
		return (int)userOutboxQueryRepository.deleteOldPublishedEvents(daysOld);
	}
}