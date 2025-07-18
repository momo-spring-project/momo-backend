package com.example.momo.domain.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserNicknameUpdateRequestDto(

	@NotBlank(message = "닉네임은 필수입니다.")
	@Size(min = 2, max = 20, message = "닉네임은 2-20자 이내여야 합니다.")
	String nickname
) {
}