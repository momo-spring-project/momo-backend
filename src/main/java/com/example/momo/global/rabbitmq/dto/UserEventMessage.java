package com.example.momo.global.rabbitmq.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

public record UserEventMessage(
	String eventId,
	String eventType,
	LocalDateTime timestamp,
	String source,
	@JsonTypeInfo(
		use = JsonTypeInfo.Id.CLASS,
		include = JsonTypeInfo.As.PROPERTY,
		property = "@class"
	)
	Object data  // 다양한 이벤트 데이터를 담기 위해 Object 사용
) {

	// 모든 User 이벤트 데이터 타입들을 내부 레코드로 정의

	// 회원탈퇴 이벤트 데이터
	public record UserWithdrawnData(
		Long userId,
		String email,
		String nickname,
		LocalDateTime withdrawnAt
	) {
	}

	// 팩토리 메서드들

	public static UserEventMessage createWithdrawn(Long userId, String email, String nickname) {
		return new UserEventMessage(
			UUID.randomUUID().toString(),
			"user.withdrawn",
			LocalDateTime.now(),
			"user-service",
			new UserWithdrawnData(userId, email, nickname, LocalDateTime.now())
		);
	}
}