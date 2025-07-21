package com.example.momo.domain.categories.controller;

import com.example.momo.domain.categories.dto.CategoryAddRequestDto;
import com.example.momo.domain.categories.dto.CategoryResponseDto;
import com.example.momo.domain.categories.dto.CategoryUpdateRequestDto;
import com.example.momo.domain.categories.service.CategoryService;
import com.example.momo.domain.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	// TODO 관리자 권한일 경우만 사용 가능하도록 변경
	// 카테고리 추가
	@PostMapping
	public ResponseEntity<ApiResponse<CategoryResponseDto>> addCategory(
		@Valid @RequestBody CategoryAddRequestDto request
		) {
		CategoryResponseDto responseData = categoryService.addCategory(request);
		ApiResponse<CategoryResponseDto> response = ApiResponse.success("카테고리 추가를 성공했습니다", responseData);
		return ResponseEntity.ok(response);
	}

	// 카테고리 조회
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