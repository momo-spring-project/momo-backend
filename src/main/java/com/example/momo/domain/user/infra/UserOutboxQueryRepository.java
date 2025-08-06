package com.example.momo.domain.user.infra;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.momo.domain.user.domain.QUserOutboxEvent;
import com.example.momo.domain.user.domain.UserOutboxEvent;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserOutboxQueryRepository {

	private final JPAQueryFactory queryFactory;

	/**
	 * 아직 발행되지 않은 Outbox 이벤트 조회
	 */
	public List<UserOutboxEvent> findUnpublishedEvents() {
		QUserOutboxEvent event = QUserOutboxEvent.userOutboxEvent;

		return queryFactory
			.selectFrom(event)
			.where(event.published.isFalse())
			.orderBy(event.createdAt.asc())
			.fetch();
	}

	/**
	 * 특정 유저 ID와 이벤트 타입에 대해 발행 상태를 true로 변경
	 */
	public void markAsPublished(Long userId, String eventType) {
		QUserOutboxEvent event = QUserOutboxEvent.userOutboxEvent;

		queryFactory
			.update(event)
			.set(event.published, true)
			.set(event.publishedAt, LocalDateTime.now())
			.where(event.userId.eq(userId)
				.and(event.eventType.eq(eventType))
				.and(event.published.isFalse()))
			.execute();
	}

	/**
	 * 일정 기간 이전에 발행된 이벤트 삭제
	 */
	public long deleteOldPublishedEvents(int daysOld) {
		QUserOutboxEvent event = QUserOutboxEvent.userOutboxEvent;
		LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);

		return queryFactory
			.delete(event)
			.where(event.published.isTrue()
				.and(event.publishedAt.lt(cutoffDate)))
			.execute();
	}
}