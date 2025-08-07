package com.example.momo.domain.meeting.domain;

import java.time.LocalDateTime;

import com.example.momo.domain.meeting.enums.ElasticsearchEventType;
import com.example.momo.global.common.entity.BaseCreateEntity;

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

@Table(name = "meeting_elasticsearch_outbox")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingElasticsearchOutbox extends BaseCreateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "meeting_id", nullable = false)
	private Long meetingId;

	@Column(nullable = false)
	private Boolean published = false;

	@Column(name = "published_at")
	private LocalDateTime publishedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type")
	private ElasticsearchEventType eventType;

	public MeetingElasticsearchOutbox(Long meetingId, ElasticsearchEventType eventType) {
		this.meetingId = meetingId;
		this.eventType = eventType;
		this.publishedAt = LocalDateTime.now();
	}

	public void markAsPublished() {
		this.published = true;
		this.publishedAt = LocalDateTime.now();
	}
}
