package com.example.momo.domain.categories.service;

import com.example.momo.domain.categories.dto.CategoryAddRequestDto;
import com.example.momo.domain.categories.dto.CategoryResponseDto;
import com.example.momo.domain.categories.dto.CategoryUpdateRequestDto;

import java.util.List;

public interface CategoryService {
	CategoryResponseDto addCategory(CategoryAddRequestDto request);

	List<CategoryResponseDto> getCategories(List<Integer> categoryIds);

	CategoryResponseDto updateCategory(Integer categoryId, CategoryUpdateRequestDto request);
}