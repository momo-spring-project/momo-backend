package com.example.momo.domain.user.application.dto;

import com.example.momo.domain.user.domain.User;

/**
 * Auth 도메인 전용 사용자 응답 DTO (비밀번호 포함)
 */
public record UserAuthResponseDto(
	Long id,
	String nickname,
	String email,
	String password, // Auth 도메인에서만 사용 (로그인 검증용)
	Double score,
	Double latitude,
	Double longitude,
	int followingCount,
	int followerCount
) {
	public UserAuthResponseDto(User user) {
		this(
			user.getId(),
			user.getNickname(),
			user.getEmail(),
			user.getPassword(), // 비밀번호 포함!
			user.getScore(),
			user.getLatitude(),
			user.getLongitude(),
			user.getFollowingCount(),
			user.getFollowerCount()
		);
	}
}