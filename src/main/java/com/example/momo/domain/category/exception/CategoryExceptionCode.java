package com.example.momo.domain.category.exception;

import com.example.momo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum CategoryExceptionCode implements ErrorCode {
	// 400
	BLANK_CATEGORY_NAME(HttpStatus.BAD_REQUEST, "Blank category name"),
	BLANK_CATEGORY_DESCRIPTION(HttpStatus.BAD_REQUEST, "Blank category description"),
	NULL_INPUT(HttpStatus.BAD_REQUEST, "Null input"),
	NOT_UPDATE_CATEGORY(HttpStatus.BAD_REQUEST, "Not update category"),

	// 403
	INSUFFICIENT_PERMISSION(HttpStatus.FORBIDDEN, "Insufficient Permission"),

	// 404
	CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Category not found"),;

	private final HttpStatus httpStatus;
	private final String message;

	CategoryExceptionCode(HttpStatus httpStatus, String message) {
		this.httpStatus = httpStatus;
		this.message = message;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public HttpStatus getStatus() {
		return this.httpStatus;
	}
}
