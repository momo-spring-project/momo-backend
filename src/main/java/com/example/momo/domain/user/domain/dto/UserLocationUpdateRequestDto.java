package com.example.momo.domain.user.domain.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record UserLocationUpdateRequestDto(
	@NotNull(message = "위도는 필수입니다")
	@DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다")
	@DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다")
	Double latitude,

	@NotNull(message = "경도는 필수입니다")
	@DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다")
	@DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다")
	Double longitude
) {
}