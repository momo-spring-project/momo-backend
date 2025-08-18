package com.example.momo.domain.user.application.dto;

import java.util.List;

import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.domain.UserCategory;

public record UserResponseDto(
	Long id,
	String nickname,
	String email,
	Double score,
	Double latitude,
	Double longitude,
	List<Integer> categoryIds,
	int followingCount,
	int followerCount,
	int ratingCount
) {
	public UserResponseDto(User user) {
		this(
			user.getId(),
			user.getNickname(),
			user.getEmail(),
			user.getScore(),
			user.getLatitude(),
			user.getLongitude(),
			user.getCategories().stream()
				.map(UserCategory::getCategoryId)
				.toList(),
			user.getFollowingCount(),
			user.getFollowerCount(),
			user.getRatings().size()
		);
	}
}