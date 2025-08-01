package com.example.momo.domain.category.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoryCreateRequestDto {
	@NotBlank
	private String categoryName;
	@NotBlank
	private String description;
}