package com.example.momo.domain.categories.controller;

import com.example.momo.domain.auth.dto.AuthUser;
import com.example.momo.domain.categories.dto.CategoryAddRequestDto;
import com.example.momo.domain.categories.dto.CategoryResponseDto;
import com.example.momo.domain.categories.dto.CategoryUpdateRequestDto;
import com.example.momo.domain.categories.service.CategoryService;
import com.example.momo.domain.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	// 카테고리 추가
	@PostMapping
	public ResponseEntity<ApiResponse<CategoryResponseDto>> addCategory(
		@AuthenticationPrincipal AuthUser authUser,
		@Valid @RequestBody CategoryAddRequestDto request
		) {
		CategoryResponseDto responseData = categoryService.addCategory(authUser, request);
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
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable Integer categoryId,
		@RequestBody CategoryUpdateRequestDto request
	) {
		CategoryResponseDto responseData = categoryService.updateCategory(authUser, categoryId, request);
		ApiResponse<CategoryResponseDto> response = ApiResponse.success("카테고리 수정을 성공했습니다", responseData);
		return ResponseEntity.ok(response);
	}
}