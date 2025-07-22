package com.example.momo.domain.user.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UserRatingCreateRequestDto(

	@NotNull
	Long meetingId,

	@NotNull
	@Min(value = 1)
	@Max(value = 5)
	Integer ratingScore
) {
}