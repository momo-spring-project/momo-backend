package com.example.momo.domain.categories.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CategoryExceptionHandler {

	@ExceptionHandler(CategoryException.class)
	public final ResponseEntity<CategoryExceptionResponseDto> handleCategoryException(CategoryException e) {
		CategoryExceptionCode exceptionCode = e.getExceptionCode();
		CategoryExceptionResponseDto responseDto =
			new CategoryExceptionResponseDto(exceptionCode.name(), e.getMessage());
		return new ResponseEntity<>(responseDto, exceptionCode.getHttpStatus());
	}
}