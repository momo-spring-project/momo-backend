package com.example.momo.domain.meeting.domain;

import java.time.LocalDateTime;

import com.example.momo.domain.meeting.enums.PaymentEventType;

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
@Table(name = "meeting_payment_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingPaymentOutbox {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "meeting_id", nullable = false)
	private Long meetingId;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false)
	private PaymentEventType eventType;

	@Column(name = "payload", nullable = false)
	private String payload;

	@Column(name = "published_at")
	private LocalDateTime publishedAt;

	@Column(name = "published", nullable = false)
	private Boolean published;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	public static MeetingPaymentOutbox create(PaymentEventType eventType, Long meetingId, String payload) {
		MeetingPaymentOutbox outbox = new MeetingPaymentOutbox();
		outbox.eventType = eventType;
		outbox.meetingId = meetingId;
		outbox.payload = payload;
		outbox.published = false;
		outbox.createdAt = LocalDateTime.now();
		return outbox;
	}

	public void markAsPublished() {
		this.published = true;
		this.publishedAt = LocalDateTime.now();
	}
}