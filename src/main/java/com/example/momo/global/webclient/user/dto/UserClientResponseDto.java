package com.example.momo.global.webclient.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserClientResponseDto {

	private Long id;
	private String nickname;
	private String email;
	private Double score;
	private Double latitude;
	private Double longitude;
	private int followingCount;
	private int followerCount;
}