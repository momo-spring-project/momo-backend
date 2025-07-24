package com.example.momo.domain.user.domain.dto;

import com.example.momo.domain.user.domain.User;

public record UserListResponseDto(
	Long id,
	String nickname,
	String email,
	Double score,
	Double latitude,
	Double longitude,
	int followingCount,
	int followerCount
) {
	public UserListResponseDto(User user) {
		this(
			user.getId(),
			user.getNickname(),
			user.getEmail(),
			user.getScore(),
			user.getLatitude(),
			user.getLongitude(),
			user.getFollowingCount(),
			user.getFollowerCount()
		);
	}

	public static UserListResponseDto from(UserResponseDto userInfo) {
		return new UserListResponseDto(
			userInfo.id(),
			userInfo.nickname(),
			userInfo.email(),
			userInfo.score(),
			userInfo.latitude(),
			userInfo.longitude(),
			userInfo.followingCount(),
			userInfo.followerCount()
		);
	}
}