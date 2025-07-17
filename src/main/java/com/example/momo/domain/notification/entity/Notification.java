package com.example.momo.domain.notification.entity;

import com.example.momo.domain.common.entity.BaseCreateEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "notification")
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
