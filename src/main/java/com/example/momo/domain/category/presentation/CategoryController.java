package com.example.momo.domain.category.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.momo.domain.category.domain.CategoryService;
import com.example.momo.domain.category.application.dto.CategoryCreateRequestDto;
import com.example.momo.domain.category.application.dto.CategoryResponseDto;
import com.example.momo.domain.category.application.dto.CategoryUpdateRequestDto;
import com.example.momo.global.common.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	// 카테고리 추가
	@PostMapping
	public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(
		@Valid @RequestBody CategoryCreateRequestDto request
	) {
		CategoryResponseDto responseData = categoryService.createCategory(request);
		ApiResponse<CategoryResponseDto> response = ApiResponse.success("카테고리 추가를 성공했습니다", responseData);
		return ResponseEntity.ok(response);
	}

	// 단일 카테고리 조회
	@GetMapping("/{categoryId}")
	public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategory(
		@PathVariable Integer categoryId
	) {
		CategoryResponseDto responseData = categoryService.getCategory(categoryId);
		ApiResponse<CategoryResponseDto> response = ApiResponse.success("단일 카테고리 조회를 성공했습니다", responseData);
		return ResponseEntity.ok(response);
	}

	// 카테고리 (다수, 전체)조회
	@GetMapping
	public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getCategories(
		@RequestParam(required = false) List<Integer> ids
	) {
		List<CategoryResponseDto> responseData = categoryService.getCategories(ids);
		ApiResponse<List<CategoryResponseDto>> response = ApiResponse.success("카테고리 조회를 성공했습니다", responseData);
		return ResponseEntity.ok(response);
	}

	// 카테고리 수정
	@PatchMapping("/{categoryId}")
	public ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(
		@PathVariable Integer categoryId,
		@RequestBody CategoryUpdateRequestDto request
	) {
		CategoryResponseDto responseData = categoryService.updateCategory(categoryId, request);
		ApiResponse<CategoryResponseDto> response = ApiResponse.success("카테고리 수정을 성공했습니다", responseData);
		return ResponseEntity.ok(response);
	}
}