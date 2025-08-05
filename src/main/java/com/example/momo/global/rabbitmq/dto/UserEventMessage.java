package com.example.momo.global.rabbitmq.dto;

import java.time.LocalDateTime;
import java.util.List;
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

	// 회원가입 이벤트 데이터
	public record UserRegisteredData(
		Long userId,
		String nickname,
		String email,
		Double latitude,
		Double longitude,
		List<Integer> categoryIds,
		LocalDateTime registeredAt
	) {
	}

	// 팔로우 이벤트 데이터
	public record UserFollowedData(
		Long followerId,
		Long followingId,
		String followerNickname,
		String followingNickname,
		LocalDateTime followedAt
	) {
	}

	// 사용자 평가 이벤트 데이터
	public record UserRatedData(
		Long reviewerId,
		Long targetUserId,
		Long meetingId,
		Integer ratingScore,
		String reviewerNickname,
		String targetUserNickname,
		LocalDateTime ratedAt
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

	public static UserEventMessage createRegistered(Long userId, String nickname, String email,
		Double latitude, Double longitude, List<Integer> categoryIds) {
		return new UserEventMessage(
			UUID.randomUUID().toString(),
			"user.registered",
			LocalDateTime.now(),
			"user-service",
			new UserRegisteredData(userId, nickname, email, latitude, longitude, categoryIds, LocalDateTime.now())
		);
	}

	public static UserEventMessage createFollowed(Long followerId, Long followingId,
		String followerNickname, String followingNickname) {
		return new UserEventMessage(
			UUID.randomUUID().toString(),
			"user.followed",
			LocalDateTime.now(),
			"user-service",
			new UserFollowedData(followerId, followingId, followerNickname, followingNickname, LocalDateTime.now())
		);
	}

	public static UserEventMessage createRated(Long reviewerId, Long targetUserId, Long meetingId,
		Integer ratingScore, String reviewerNickname, String targetUserNickname) {
		return new UserEventMessage(
			UUID.randomUUID().toString(),
			"user.rated",
			LocalDateTime.now(),
			"user-service",
			new UserRatedData(reviewerId, targetUserId, meetingId, ratingScore,
				reviewerNickname, targetUserNickname, LocalDateTime.now())
		);
	}
}