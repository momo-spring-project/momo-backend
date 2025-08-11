package com.example.momo.global.rabbitmq.dto;

import java.time.LocalDateTime;

/**
 * 유저 도메인 이벤트 데이터 모음
 * EventWrapper와 함께 사용되는 데이터 클래스들을 정의
 */
public class UserEventMessage {

	/**
	 * 회원탈퇴 이벤트 데이터
	 */
	public record UserWithdrawnData(
		Long userId,
		String email,
		String nickname,
		LocalDateTime withdrawnAt
	) {
		public UserWithdrawnData(Long userId, String email, String nickname) {
			this(userId, email, nickname, LocalDateTime.now());
		}
	}

	/**
	 * 팔로우 이벤트 데이터
	 */
	public record UserFollowedData(
		Long followerId,      // 팔로우한 사람
		Long followingId,     // 팔로우 당한 사람
		String followerNickname,
		LocalDateTime followedAt
	) {
		public UserFollowedData(Long followerId, Long followingId, String followerNickname) {
			this(followerId, followingId, followerNickname, LocalDateTime.now());
		}
	}
}