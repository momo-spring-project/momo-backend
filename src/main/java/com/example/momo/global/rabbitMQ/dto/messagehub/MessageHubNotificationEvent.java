package com.example.momo.global.rabbitMQ.dto.messagehub;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class MessageHubNotificationEvent {
	private Long userId;
	private Long targetId;
	private String content;
	private String type;
	@Nullable
	private Long notificationId;

	public void updateNotificationId(Long notificationId) {
		this.notificationId = notificationId;
	}

}
