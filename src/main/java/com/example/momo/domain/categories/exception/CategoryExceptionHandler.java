package com.example.momo.domain.categories.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CategoryExceptionHandler {

	@ExceptionHandler(CategoryException.class)
	public final ResponseEntity<CategoryExceptionResponseDto> handleCategoryException(CategoryException e) {
		CategoryExceptionCode exceptionCode = e.getExceptionCode();
		CategoryExceptionResponseDto responseDto =
			new CategoryExceptionResponseDto(exceptionCode.name(), e.getMessage());
		log.info("Exception caught in CategoryExceptionHandler");
		return new ResponseEntity<>(responseDto, exceptionCode.getStatus());
	}
}