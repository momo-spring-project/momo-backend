package com.example.momo.domain.user.domain.dto;

import com.example.momo.domain.user.domain.User;

public record UserLocationResponseDto(
	Double latitude,
	Double longitude
) {
	public UserLocationResponseDto(User user) {
		this(user.getLatitude(), user.getLongitude());
	}
}