package com.example.momo.global.infrastructure.client.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * UserClient에서 Auth 도메인과 통신할 때 사용하는 DTO (비밀번호 포함)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthClientResponseDto {

	private Long id;
	private String nickname;
	private String email;
	private String password; // Auth 도메인 전용 (로그인 검증용)
	private Double score;
	private Double latitude;
	private Double longitude;
	private int followingCount;
	private int followerCount;
}
