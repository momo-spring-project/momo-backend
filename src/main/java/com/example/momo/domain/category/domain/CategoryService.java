package com.example.momo.domain.category.domain;

import java.util.List;

import com.example.momo.domain.category.application.dto.CategoryCreateRequestDto;
import com.example.momo.domain.category.application.dto.CategoryResponseDto;
import com.example.momo.domain.category.application.dto.CategoryUpdateRequestDto;

public interface CategoryService {
	CategoryResponseDto createCategory(CategoryCreateRequestDto request);

	List<CategoryResponseDto> getCategories(List<Integer> categoryIds);

	CategoryResponseDto updateCategory(Integer categoryId, CategoryUpdateRequestDto request);

	CategoryResponseDto getCategory(Integer categoryId);

	void deleteCategory(Integer categoryId);
}