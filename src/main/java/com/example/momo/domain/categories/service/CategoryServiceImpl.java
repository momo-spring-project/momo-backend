package com.example.momo.domain.categories.service;

import com.example.momo.domain.categories.dto.CategoryUpdateRequestDto;
import com.example.momo.domain.categories.exception.CategoryException;
import com.example.momo.domain.categories.exception.CategoryExceptionCode;
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

	// 단일 카테고리 조회
	@Override
	public CategoryResponseDto getCategory(Integer categoryId) {

		Category category = categoryRepository.findById(categoryId)
			.orElseThrow(() -> new CategoryException(CategoryExceptionCode.CATEGORY_NOT_FOUND));

		return new CategoryResponseDto(category);
	}

	// 카테고리 (다수, 전체)조회
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

	// Category 수정
	@Override
	@Transactional
	public CategoryResponseDto updateCategory(Integer id, CategoryUpdateRequestDto request) {

		// 입력 잘못됐을 경우 예외처리(공백 포함)
		if(request.getCategoryName() != null && !StringUtils.hasText(request.getCategoryName())) {
			throw new CategoryException(CategoryExceptionCode.BLANK_CATEGORY_NAME);
		}
		if(request.getDescription() != null && !StringUtils.hasText(request.getDescription())) {
			throw new CategoryException(CategoryExceptionCode.BLANK_CATEGORY_DESCRIPTION);
		}

		// 입력이 모두 null 일 경우 예외처리
		if(request.getCategoryName() == null && request.getDescription() == null) {
			throw new CategoryException(CategoryExceptionCode.NULL_INPUT);
		}

		Category category = categoryRepository.findById(id)
			.orElseThrow(() -> new CategoryException(CategoryExceptionCode.CATEGORY_NOT_FOUND));

		// 변경사항 없을 경우 예외처리
		if(category.getName().equals(request.getCategoryName()) && category.getDescription().equals(request.getDescription())) {
			throw new CategoryException(CategoryExceptionCode.NOT_UPDATE_CATEGORY);
		}

		if(request.getCategoryName() != null) category.updateName(request.getCategoryName());
		if(request.getDescription() != null) category.updateDescription(request.getDescription());

		return new CategoryResponseDto(category);
	}
}