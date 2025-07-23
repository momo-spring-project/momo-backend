package com.example.momo.domain.user.domain.dto;

import java.util.List;

/**
 * 팔로우/팔로워 목록 응답 DTO
 * 페이징 정보와 함께 사용자들의 팔로우 관련 정보를 제공
 */
public record UserFollowListResponseDto(
	List<UserFollowInfoResponseDto> data,
	int totalCount,
	int currentPage,
	int pageSize,
	boolean hasNext
) {
	// 기존 코드와의 호환성을 위한 정적 팩토리 메서드
	public static UserFollowListResponseDto of(
		List<UserFollowInfoResponseDto> users,
		int totalCount,
		int currentPage,
		int pageSize
	) {
		boolean hasNext = (long) currentPage * pageSize < totalCount;
		return new UserFollowListResponseDto(users, totalCount, currentPage, pageSize, hasNext);
	}

	// 기존 users 필드 접근을 위한 호환성 메서드 (Deprecated 처리)
	@Deprecated
	public List<UserFollowInfoResponseDto> users() {
		return data;
	}
}