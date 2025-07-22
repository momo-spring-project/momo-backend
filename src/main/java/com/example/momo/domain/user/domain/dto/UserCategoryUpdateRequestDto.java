package com.example.momo.domain.user.domain.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record UserCategoryUpdateRequestDto(

	@NotNull
	List<Integer> categoryIds
) {
}