package com.example.momo.domain.categories.service;

import com.example.momo.domain.categories.dto.CategoryUpdateRequestDto;
import com.example.momo.domain.categories.repository.CategoryRepository;
import com.example.momo.domain.categories.dto.CategoryAddRequestDto;
import com.example.momo.domain.categories.dto.CategoryFindRequestDto;
import com.example.momo.domain.categories.dto.CategoryResponseDto;
import com.example.momo.domain.categories.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService{

	private final CategoryRepository categoryRepository;

	// Category 추가
	@Override
	@Transactional
	public CategoryResponseDto addCategory(CategoryAddRequestDto request) {

		Category category = new Category(request.getCategoryName(), request.getDescription());
		Category savedCategory = categoryRepository.save(category);

		return new CategoryResponseDto(savedCategory.getId(), savedCategory.getName(), category.getDescription());
	}

	// 전체 Category 목록 조회
	@Override
	@Transactional(readOnly = true)
	public List<CategoryResponseDto> getAllCategories() {

		List<Category> categories = categoryRepository.findAll();

		return categories.stream()
			.map(CategoryResponseDto::new)
			.toList();
	}

	// Category ID 이용해서 조회
	@Override
	@Transactional(readOnly = true)
	public List<CategoryResponseDto> getCategoriesByIds(CategoryFindRequestDto request) {

		List<Category> categories = categoryRepository.findAllByIdIn(request.getCategoryIds());

		return categories.stream()
			.map(CategoryResponseDto::new)
			.toList();
	}

	// TODO 예외처리 변경
	// Category 수정
	@Override
	@Transactional
	public CategoryResponseDto updateCategory(Integer id, CategoryUpdateRequestDto request) {

		Category category = categoryRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Category not found"));

		// 변경사항 없을 경우 예외처리
		if(request.getCategoryName() == null && request.getDescription() == null) {
			throw new IllegalArgumentException("No Change");
		}

		if(request.getCategoryName() != null) category.updateName(request.getCategoryName());
		if(request.getDescription() != null) category.updateDescription(request.getDescription());

		return new CategoryResponseDto(category);
	}
}