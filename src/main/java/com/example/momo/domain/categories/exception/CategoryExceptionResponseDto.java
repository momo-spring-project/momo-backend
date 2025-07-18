package com.example.momo.domain.categories.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryExceptionResponseDto {
	public String code;
	public String message;
}
