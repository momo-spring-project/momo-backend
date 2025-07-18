package com.example.momo.domain.user.domain.dto;

import java.util.List;

import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.domain.UserCategory;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponseDto {
	private Long id;
	private String nickname;
	private String email;
	private Double score;
	private Double latitude;
	private Double longitude;
	private List<Integer> categoryIds;
	private int followingCount;
	private int followerCount;
	private int ratingCount;

	public UserInfoResponseDto(User user) {
		this.id = user.getId();
		this.nickname = user.getNickname();
		this.email = user.getEmail();
		this.score = user.getScore();
		this.latitude = user.getLatitude();
		this.longitude = user.getLongitude();
		this.categoryIds = user.getCategories().stream()
			.map(UserCategory::getCategoryId)
			.toList();
		this.followingCount = user.getFollowings().size();
		this.followerCount = 0; // 추후 팔로워 조회 로직 추가 필요
		this.ratingCount = user.getRatings().size();
	}
}