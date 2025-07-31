package com.example.momo.domain.category.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoryUpdateRequestDto {
	private String categoryName;
	private String description;
}