package com.example.momo.domain.user.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPasswordUpdateRequestDto(

	@NotBlank
	String currentPassword,

	@NotBlank
	@Size(min = 8, max = 20)
	String newPassword,

	@NotBlank
	String confirmPassword
) {
}