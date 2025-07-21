package com.example.momo.domain.categories.exception;

import com.example.momo.global.exception.BaseException;
import lombok.Getter;

@Getter
public class CategoryException extends BaseException {

	private final CategoryExceptionCode exceptionCode;
	private final String exceptionMessage;

	public CategoryException(CategoryExceptionCode exceptionCode) {
		super(exceptionCode);
		this.exceptionCode = exceptionCode;
		this.exceptionMessage = exceptionCode.getMessage();
	}

	public CategoryException(CategoryExceptionCode exceptionCode, String exceptionMessage) {
		super(exceptionCode);
		this.exceptionCode = exceptionCode;
		this.exceptionMessage = exceptionMessage;
	}
}