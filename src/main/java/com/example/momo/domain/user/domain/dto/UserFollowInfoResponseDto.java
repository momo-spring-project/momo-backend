package com.example.momo.domain.user.domain.dto;

import com.example.momo.domain.user.domain.User;

public record UserFollowInfoResponseDto(
	Long id,
	String nickname,
	Integer followerCount,
	Integer followingCount,
	Double score
) {
	public UserFollowInfoResponseDto(User user) {
		this(
			user.getId(),
			user.getNickname(),
			user.getFollowerCount(),
			user.getFollowingCount(),
			user.getScore()
		);
	}
}