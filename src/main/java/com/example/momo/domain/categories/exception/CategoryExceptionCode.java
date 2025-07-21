package com.example.momo.domain.categories.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CategoryExceptionCode{
	// 400
	BLANK_CATEGORY_NAME(HttpStatus.BAD_REQUEST, "Blank category name"),
	BLANK_CATEGORY_DESCRIPTION(HttpStatus.BAD_REQUEST, "Blank category description"),
	NULL_INPUT(HttpStatus.BAD_REQUEST, "Null input"),
	NOT_UPDATE_CATEGORY(HttpStatus.BAD_REQUEST, "Not update category"),

	// 404
	CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Category not found"),;

	private final HttpStatus httpStatus;
	private final String message;

	CategoryExceptionCode(HttpStatus httpStatus, String message) {
		this.httpStatus = httpStatus;
		this.message = message;
	}
}
