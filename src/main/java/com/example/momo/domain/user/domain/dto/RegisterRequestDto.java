package com.example.momo.domain.user.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequestDto(
	@NotBlank
	String nickname,

	@Email
	@NotBlank
	String email,

	@NotBlank
	String password,

	@NotNull
	Double latitude,

	@NotNull
	Double longitude
) {
}
