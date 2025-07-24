package com.example.momo.domain.user.domain.dto;

import com.example.momo.domain.user.domain.User;

public record UserFollowResponseDto(
	Long id,
	String nickname,
	Integer followerCount,
	Integer followingCount,
	Double score
) {
	public UserFollowResponseDto(User user) {
		this(
			user.getId(),
			user.getNickname(),
			user.getFollowerCount(),
			user.getFollowingCount(),
			user.getScore()
		);
	}
}