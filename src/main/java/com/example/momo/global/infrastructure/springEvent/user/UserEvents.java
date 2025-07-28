package com.example.momo.global.infrastructure.springEvent.user;

import java.time.LocalDateTime;
import java.util.List;

public class UserEvents {
	// === 생명주기 이벤트 ===

	/**
	 * 회원가입 완료 이벤트
	 */
	public record UserRegistered(
		Long userId,
		String nickname,
		String email,
		Double latitude,
		Double longitude,
		LocalDateTime registeredAt
	) {
	}

	/**
	 * 회원탈퇴 이벤트
	 */
	public record UserWithdrawn(
		Long userId,
		String email,
		String nickname,
		LocalDateTime withdrawnAt
	) {
	}

	// === 팔로우 관련 이벤트 ===

	/**
	 * 팔로우 생성 이벤트
	 */
	public record UserFollowed(
		Long followerId,
		Long followingId,
		String followerNickname,
		String followingNickname,
		LocalDateTime followedAt
	) {
	}

	/**
	 * 언팔로우 이벤트
	 */
	public record UserUnfollowed(
		Long followerId,
		Long followingId,
		String followerNickname,
		String followingNickname,
		LocalDateTime unfollowedAt
	) {
	}

	// === 프로필 변경 이벤트 ===

	/**
	 * 닉네임 변경 이벤트
	 */
	public record NicknameChanged(
		Long userId,
		String oldNickname,
		String newNickname,
		LocalDateTime changedAt
	) {
	}

	/**
	 * 비밀번호 변경 이벤트
	 */
	public record PasswordChanged(
		Long userId,
		String email,
		LocalDateTime changedAt
	) {
	}

	/**
	 * 위치 정보 변경 이벤트
	 */
	public record LocationChanged(
		Long userId,
		String nickname,
		Double oldLatitude,
		Double oldLongitude,
		Double newLatitude,
		Double newLongitude,
		LocalDateTime changedAt
	) {
	}

	/**
	 * 관심 카테고리 변경 이벤트
	 */
	public record CategoriesChanged(
		Long userId,
		String nickname,
		List<Integer> oldCategoryIds,
		List<Integer> newCategoryIds,
		LocalDateTime changedAt
	) {
	}

	// === 평가 관련 이벤트 ===

	/**
	 * 사용자 평가 생성 이벤트
	 */
	public record UserRated(
		Long reviewerId,
		Long targetUserId,
		Long meetingId,
		Integer ratingScore,
		String reviewerNickname,
		String targetUserNickname,
		LocalDateTime ratedAt
	) {
	}

	/**
	 * 사용자 점수 변경 이벤트
	 */
	public record ScoreChanged(
		Long userId,
		String nickname,
		Double oldScore,
		Double newScore,
		String changeReason, // "평가 반영", "활동도 업데이트" 등
		LocalDateTime changedAt
	) {
	}
}