package com.example.momo.domain.user.domain.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record UserCategoryUpdateRequestDto(

	@NotNull(message = "카테고리 ID 목록은 필수입니다.")
	List<Integer> categoryIds
) {
}