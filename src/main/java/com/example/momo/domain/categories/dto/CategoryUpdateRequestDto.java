package com.example.momo.domain.categories.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoryUpdateRequestDto {
	@NotEmpty
	private String categoryName;
	@NotEmpty
	private String description;
}