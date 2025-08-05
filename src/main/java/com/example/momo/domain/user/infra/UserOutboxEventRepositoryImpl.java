package com.example.momo.domain.user.infra;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.momo.domain.user.domain.UserOutboxEvent;
import com.example.momo.domain.user.domain.UserOutboxEventRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * 사용자 아웃박스 이벤트 저장소 구현체
 */
@Repository
public class UserOutboxEventRepositoryImpl implements UserOutboxEventRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public void save(UserOutboxEvent outboxEvent) {
		entityManager.persist(outboxEvent);
	}

	@Override
	public void markAsPublished(Long userId, String eventType) {
		String jpql = """
			UPDATE UserOutboxEvent u 
			SET u.published = true, u.publishedAt = CURRENT_TIMESTAMP 
			WHERE u.userId = :userId 
			AND u.eventType = :eventType 
			AND u.published = false
			""";

		entityManager.createQuery(jpql)
			.setParameter("userId", userId)
			.setParameter("eventType", eventType)
			.executeUpdate();
	}

	@Override
	public List<UserOutboxEvent> findUnpublishedEvents() {
		String jpql = """
			SELECT u FROM UserOutboxEvent u 
			WHERE u.published = false 
			ORDER BY u.createdAt ASC
			""";

		return entityManager.createQuery(jpql, UserOutboxEvent.class)
			.getResultList();
	}

	@Override
	public List<UserOutboxEvent> findUnpublishedEventsWithRetryCountLessThan(int maxRetryCount) {
		String jpql = """
			SELECT u FROM UserOutboxEvent u 
			WHERE u.published = false 
			AND u.retryCount < :maxRetryCount 
			ORDER BY u.createdAt ASC
			""";

		return entityManager.createQuery(jpql, UserOutboxEvent.class)
			.setParameter("maxRetryCount", maxRetryCount)
			.getResultList();
	}

	@Override
	public void incrementRetryCount(Long outboxEventId) {
		String jpql = """
			UPDATE UserOutboxEvent u 
			SET u.retryCount = u.retryCount + 1, u.lastRetryAt = CURRENT_TIMESTAMP 
			WHERE u.id = :id
			""";

		entityManager.createQuery(jpql)
			.setParameter("id", outboxEventId)
			.executeUpdate();
	}

	@Override
	public int deleteOldPublishedEvents(int daysOld) {
		String jpql = """
			DELETE FROM UserOutboxEvent u 
			WHERE u.published = true 
			AND u.publishedAt < :cutoffDate
			""";

		LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);

		return entityManager.createQuery(jpql)
			.setParameter("cutoffDate", cutoffDate)
			.executeUpdate();
	}
}