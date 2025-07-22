package com.example.momo.domain.user.domain.dto;

import java.util.List;

public record UserFollowListResponseDto(
	List<UserFollowInfoResponseDto> users,
	int totalCount,
	int currentPage,
	int pageSize
) {
}