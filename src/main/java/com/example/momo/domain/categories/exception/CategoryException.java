package com.example.momo.domain.categories.exception;

import lombok.Getter;

@Getter
public class CategoryException extends RuntimeException {

	private final CategoryExceptionCode exceptionCode;
	private final String exceptionMessage;

	public CategoryException(CategoryExceptionCode exceptionCode) {
		this.exceptionCode = exceptionCode;
		this.exceptionMessage = exceptionCode.getMessage();
	}

	public CategoryException(CategoryExceptionCode exceptionCode, String exceptionMessage) {
		this.exceptionCode = exceptionCode;
		this.exceptionMessage = exceptionMessage;
	}
}