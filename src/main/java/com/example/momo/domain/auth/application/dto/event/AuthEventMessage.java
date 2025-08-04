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

	public record UserRegisteredData(
		Long userId,
		String email,
		String nickname,
		LocalDateTime registeredAt
	) {
	}

	// 향후 Auth 도메인 자체 이벤트들도 여기에 추가 가능
	public record SocialLoginLinkedData(
		Long userId,
		String provider,
		String providerId,
		LocalDateTime linkedAt
	) {
	}

	// 타입 안전 캐스팅 헬퍼 메서드들
	public UserWithdrawnData getUserWithdrawnData() {
		return (UserWithdrawnData)data;
	}

	public UserRegisteredData getUserRegisteredData() {
		return (UserRegisteredData)data;
	}

	public SocialLoginLinkedData getSocialLoginLinkedData() {
		return (SocialLoginLinkedData)data;
	}
}