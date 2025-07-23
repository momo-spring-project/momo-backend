package com.example.momo.domain.user.domain.dto;

import com.example.momo.domain.user.domain.User;

public record UserInfoListResponseDto(
	Long id,
	String nickname,
	String email,
	Double score,
	Double latitude,
	Double longitude,
	int followingCount,
	int followerCount
) {
	public UserInfoListResponseDto(User user) {
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

	public static UserInfoListResponseDto from(UserInfoResponseDto userInfo) {
		return new UserInfoListResponseDto(
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