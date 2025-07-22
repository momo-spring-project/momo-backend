package com.example.momo.domain.category.application;

import com.example.momo.domain.category.domain.dto.CategoryAddRequestDto;
import com.example.momo.domain.category.domain.dto.CategoryResponseDto;
import com.example.momo.domain.category.domain.dto.CategoryUpdateRequestDto;

import java.util.List;

public interface CategoryService {
	CategoryResponseDto addCategory(CategoryAddRequestDto request);

	List<CategoryResponseDto> getCategories(List<Integer> categoryIds);

	CategoryResponseDto updateCategory(Integer categoryId, CategoryUpdateRequestDto request);

	CategoryResponseDto getCategory(Integer categoryId);
}