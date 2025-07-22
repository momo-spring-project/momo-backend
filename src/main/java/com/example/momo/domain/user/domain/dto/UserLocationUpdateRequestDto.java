package com.example.momo.domain.user.domain.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record UserLocationUpdateRequestDto(
	@NotNull
	@DecimalMin(value = "-90.0")
	@DecimalMax(value = "90.0")
	Double latitude,

	@NotNull
	@DecimalMin(value = "-180.0")
	@DecimalMax(value = "180.0")
	Double longitude
) {
}