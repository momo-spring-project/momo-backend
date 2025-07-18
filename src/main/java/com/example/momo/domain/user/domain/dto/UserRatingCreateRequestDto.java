package com.example.momo.domain.user.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UserRatingCreateRequestDto(

	@NotNull(message = "모임 ID는 필수입니다.")
	Long meetingId,

	@NotNull(message = "평점은 필수입니다.")
	@Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
	@Max(value = 5, message = "평점은 5점 이하여야 합니다.")
	Integer ratingScore
) {
}