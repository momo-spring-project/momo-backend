package com.example.momo.domain.categories.controller;

import com.example.momo.domain.categories.dto.CategoryAddRequestDto;
import com.example.momo.domain.categories.dto.CategoryFindRequestDto;
import com.example.momo.domain.categories.dto.CategoryResponseDto;
import com.example.momo.domain.categories.dto.CategoryUpdateRequestDto;
import com.example.momo.domain.categories.service.CategoryService;
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
	public ResponseEntity<CategoryResponseDto> addCategory(
		@Valid @RequestBody CategoryAddRequestDto request
		) {
		CategoryResponseDto response = categoryService.addCategory(request);
		return ResponseEntity.ok(response);
	}

	// 카테고리 조회
	@GetMapping
	public ResponseEntity<List<CategoryResponseDto>> getCategories(
		@RequestParam(required = false) List<Integer> ids
	) {
		List<CategoryResponseDto> response = categoryService.getCategories(ids);
		return ResponseEntity.ok(response);
	}

	// 카테고리 수정
	@PatchMapping("/{categoryId}")
	public ResponseEntity<CategoryResponseDto> updateCategory(
		@PathVariable Integer categoryId,
		@Valid @RequestBody CategoryUpdateRequestDto request
	) {
		CategoryResponseDto response = categoryService.updateCategory(categoryId, request);
		return ResponseEntity.ok(response);
	}
}