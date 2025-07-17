package com.example.momo.domain.categories.service;

import com.example.momo.domain.categories.dto.CategoryUpdateRequestDto;
import com.example.momo.domain.categories.repository.CategoryRepository;
import com.example.momo.domain.categories.dto.CategoryAddRequestDto;
import com.example.momo.domain.categories.dto.CategoryResponseDto;
import com.example.momo.domain.categories.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

	// 카테고리 조회
	@Override
	@Transactional(readOnly = true)
	public List<CategoryResponseDto> getCategories(List<Integer> categoryIds) {

		// ID 목록 없으면 전체 조회, 있으면 목록 조회
		List<Category> categories;
		if(categoryIds == null) {
			categories = categoryRepository.findAll();
		} else {
			categories = categoryRepository.findAllByIdIn(categoryIds);
		}

		return categories.stream()
			.map(CategoryResponseDto::new)
			.toList();
	}

	// TODO 예외처리 변경
	// Category 수정
	@Override
	@Transactional
	public CategoryResponseDto updateCategory(Integer id, CategoryUpdateRequestDto request) {

		// 입력 잘못됐을 경우 예외처리(공백 포함)
		if(request.getCategoryName() != null && !StringUtils.hasText(request.getCategoryName())) {
			throw new IllegalArgumentException("Blank category name");
		}
		if(request.getDescription() != null && !StringUtils.hasText(request.getDescription())) {
			throw new IllegalArgumentException("Blank category description");
		}

		// 변경사항 없을 경우 예외처리
		if(request.getCategoryName() == null && request.getDescription() == null) {
			throw new IllegalArgumentException("No Change(null)");
		}

		Category category = categoryRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Category not found"));

		if(category.getName().equals(request.getCategoryName()) && category.getDescription().equals(request.getDescription())) {
			throw new IllegalArgumentException("No Change(same)");
		}

		if(request.getCategoryName() != null) category.updateName(request.getCategoryName());
		if(request.getDescription() != null) category.updateDescription(request.getDescription());

		return new CategoryResponseDto(category);
	}
}