package com.example.momo.domain.user.application.dto;

import java.util.List;

import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.domain.UserCategory;

public record UserCategoryUpdateResponseDto(
	List<Integer> categoryIds
) {
	public UserCategoryUpdateResponseDto(User user) {
		this(user.getCategories().stream()
			.map(UserCategory::getCategoryId)
			.toList());
	}
}
