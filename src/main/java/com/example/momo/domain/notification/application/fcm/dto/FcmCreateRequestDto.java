package com.example.momo.domain.notification.application.fcm.dto;

import com.example.momo.domain.notification.domain.FcmToken;
import com.example.momo.domain.notification.enums.PlatformType;

public record FcmCreateRequestDto(String token, String deviceId, PlatformType platformType) {

	public FcmToken toEntity(Long userId) {
		return FcmToken.builder()
			.userId(userId)
			.token(token)
			.deviceId(deviceId)
			.platformType(platformType)
			.build();
	}
}
