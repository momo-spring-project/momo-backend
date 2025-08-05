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
	 * 아직 발행되지 않고 재시도 횟수가 maxRetryCount 미만인 Outbox 이벤트 조회
	 *
	 * @param maxRetryCount 최대 재시도 횟수
	 * @return 조건에 맞는 Outbox 이벤트 목록
	 */
	public List<UserOutboxEvent> findUnpublishedEventsWithRetryCountLessThan(int maxRetryCount) {
		QUserOutboxEvent event = QUserOutboxEvent.userOutboxEvent;

		return queryFactory
			.selectFrom(event)
			.where(event.published.isFalse()
				.and(event.retryCount.lt(maxRetryCount)))
			.orderBy(event.createdAt.asc())
			.fetch();
	}

	/**
	 * 특정 유저 ID와 이벤트 타입에 대해 발행 상태를 true로 변경
	 *
	 * @param userId 유저 ID
	 * @param eventType 이벤트 타입
	 * @return 업데이트 된 레코드 수
	 */
	public long markAsPublished(Long userId, String eventType) {
		QUserOutboxEvent event = QUserOutboxEvent.userOutboxEvent;

		return queryFactory
			.update(event)
			.set(event.published, true)
			.set(event.publishedAt, LocalDateTime.now())
			.where(event.userId.eq(userId)
				.and(event.eventType.eq(eventType))
				.and(event.published.isFalse()))
			.execute();
	}

	/**
	 * 재시도 횟수를 1 증가시키고 마지막 재시도 시간 갱신
	 *
	 * @param outboxEventId Outbox 이벤트 ID
	 */
	public void incrementRetryCount(Long outboxEventId) {
		QUserOutboxEvent event = QUserOutboxEvent.userOutboxEvent;

		queryFactory
			.update(event)
			.set(event.retryCount, event.retryCount.add(1))
			.set(event.lastRetryAt, LocalDateTime.now())
			.where(event.id.eq(outboxEventId))
			.execute();
	}

	/**
	 * 일정 기간 이전에 발행된 이벤트 삭제
	 *
	 * @param daysOld 며칠 이전 이벤트까지 삭제할지 기준
	 * @return 삭제된 이벤트 수
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