package com.example.momo.domain.payment.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.momo.domain.payment.enums.OutboxStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentOutbox {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String eventType;

	@Column(nullable = false)
	private String aggregateId;

	@Column(nullable = false)
	private String routingKey;

	@Column(nullable = false, columnDefinition = "MEDIUMTEXT")
	private String payload;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "published_at")
	private LocalDateTime publishedAt;

	@Column(nullable = false)
	private Boolean published = false;

	@Column(nullable = false)
	private Integer retryCount = 0;

	@Column(unique = true)
	private String correlationId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OutboxStatus status = OutboxStatus.PENDING;

	@Column(columnDefinition = "TEXT")
	private String failureReason;

	/** 다음 재시도 시각 - 지수 백오프 계산값 저장 */
	@Column(name = "next_retry_at")
	private LocalDateTime nextRetryAt;

	public static PaymentOutbox create(String eventType, String aggregateId,
		String routingKey, String payload) {
		PaymentOutbox outbox = new PaymentOutbox();
		outbox.eventType = eventType;
		outbox.aggregateId = aggregateId;
		outbox.routingKey = routingKey;
		outbox.payload = payload;
		outbox.createdAt = LocalDateTime.now();
		outbox.updatedAt = outbox.createdAt;
		outbox.correlationId = UUID.randomUUID().toString();
		outbox.status = OutboxStatus.PENDING;
		outbox.retryCount = 0;
		outbox.published = false;
		return outbox;
	}

	/** 발행 성공 처리 */
	public void markAsPublished() {
		this.status = OutboxStatus.PUBLISHED;
		this.published = true;
		this.publishedAt = LocalDateTime.now();
		this.updatedAt = this.publishedAt;
		this.nextRetryAt = null;

	}

	/**
	 * 발행 실패 처리 (지수 백오프 적용)
	 * 베이스 10초 * 2^(retryCount-1) (최대 300초)
	 */
	public void markAsFailed(String reason) {
		this.status = OutboxStatus.FAILED;
		this.retryCount++;
		this.failureReason = reason;
		this.updatedAt = LocalDateTime.now();

		long baseSeconds = 10;
		long delaySeconds = Math.min(
			baseSeconds * (long)Math.pow(2, Math.max(0, retryCount - 1)),
			300
		);
		this.nextRetryAt = LocalDateTime.now().plusSeconds(delaySeconds);
	}

	/** DLQ 처리 */
	public void markAsDeadLettered() {
		this.status = OutboxStatus.DEAD_LETTERED;
		this.updatedAt = LocalDateTime.now();
		this.nextRetryAt = null;

	}
}
