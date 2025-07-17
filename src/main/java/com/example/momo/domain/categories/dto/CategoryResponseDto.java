package com.example.momo.domain.categories.dto;

import com.example.momo.domain.categories.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryResponseDto {
	private Integer id;
	private String categoryName;
	private String description;

	public CategoryResponseDto(Category category) {
		this.id = category.getId();
		this.categoryName = category.getName();
		this.description = category.getDescription();
	}
}