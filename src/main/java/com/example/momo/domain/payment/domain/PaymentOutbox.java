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

	@Column(nullable = false, length = 65535)
	private String payload;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	private LocalDateTime publishedAt;

	@Column(nullable = false)
	private Boolean published = false;

	private Integer retryCount = 0;

	@Column(unique = true)
	private String correlationId;

	@Enumerated(EnumType.STRING)
	private OutboxStatus status = OutboxStatus.PENDING;

	private String failureReason;

	public static PaymentOutbox create(String eventType, String aggregateId,
		String routingKey, String payload) {
		PaymentOutbox outbox = new PaymentOutbox();
		outbox.eventType = eventType;
		outbox.aggregateId = aggregateId;
		outbox.routingKey = routingKey;
		outbox.payload = payload;
		outbox.createdAt = LocalDateTime.now();
		outbox.correlationId = UUID.randomUUID().toString();
		return outbox;
	}

	public void markAsPublished() {
		this.status = OutboxStatus.PUBLISHED;
		this.published = true;
		this.publishedAt = LocalDateTime.now();
	}

	public void markAsFailed(String reason) {
		this.status = OutboxStatus.FAILED;
		this.retryCount++;
		this.failureReason = reason;
	}

	public void markAsDeadLettered() {
		this.status = OutboxStatus.DEAD_LETTERED;
	}
}