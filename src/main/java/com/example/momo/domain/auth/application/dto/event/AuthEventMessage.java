package com.example.momo.domain.auth.application.dto.event;

import java.time.LocalDateTime;

public record AuthEventMessage(
	String eventId,
	String eventType,
	LocalDateTime timestamp,
	String source,
	Object data
) {

	// Auth 도메인에서 필요한 User 이벤트 데이터만 정의
	public record UserWithdrawnData(
		Long userId,
		String email,
		String nickname,
		LocalDateTime withdrawnAt
	) {
	}

	// 타입 안전 캐스팅 헬퍼 메서드들
	public UserWithdrawnData getUserWithdrawnData() {
		return (UserWithdrawnData)data;
	}
}