package com.example.momo.domain.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserNicknameUpdateRequestDto(

	@NotBlank
	@Size(min = 2, max = 20)
	String nickname
) {
}