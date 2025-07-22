package com.example.momo.domain.notification.domain;

import com.example.momo.global.common.entity.BaseCreateEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(
	name = "notifications",
	indexes = {
		@Index(name = "idx_notification_user_id_id_desc", columnList = "user_id, id DESC")
	}
)
@Getter
@Entity
@NoArgsConstructor
public class Notification extends BaseCreateEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, name = "user_id")
	private Long userId;

	@Column(nullable = false, name = "meeting_id")
	private Long meetingId;

	@Column(nullable = false, name = "content")
	private String content;

	public Notification(Long userId, Long meetingId, String content) {
		this.userId = userId;
		this.meetingId = meetingId;
		this.content = content;
	}
}
