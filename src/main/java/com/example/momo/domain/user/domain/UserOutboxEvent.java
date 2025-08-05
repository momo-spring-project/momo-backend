package com.example.momo.domain.user.domain;

import java.time.LocalDateTime;

import com.example.momo.global.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 도메인 아웃박스 이벤트 엔티티
 * 이벤트 발행 실패 시 데이터 일관성을 보장하기 위한 아웃박스 패턴 구현
 */
@Table(name = "user_outbox_events")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserOutboxEvent extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, name = "user_id")
	private Long userId;

	@Column(nullable = false, name = "event_type")
	private String eventType;

	@Column(columnDefinition = "TEXT", name = "event_data")
	private String eventData;

	@Column(nullable = false, name = "published")
	private Boolean published = false;

	@Column(name = "published_at")
	private LocalDateTime publishedAt;

	@Column(name = "retry_count")
	private Integer retryCount = 0;

	@Column(name = "last_retry_at")
	private LocalDateTime lastRetryAt;

	// 정적 팩토리 메서드
	public static UserOutboxEvent create(Long userId, String eventType, String eventData) {
		UserOutboxEvent outboxEvent = new UserOutboxEvent();
		outboxEvent.userId = userId;
		outboxEvent.eventType = eventType;
		outboxEvent.eventData = eventData;
		return outboxEvent;
	}

	// 발행 완료 처리
	public void markAsPublished() {
		this.published = true;
		this.publishedAt = LocalDateTime.now();
	}

	// 재시도 처리
	public void incrementRetryCount() {
		this.retryCount++;
		this.lastRetryAt = LocalDateTime.now();
	}
}
