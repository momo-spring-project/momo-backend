package com.example.momo.domain.notification.domain;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository {
	void save(FcmToken token);

	List<FcmToken> findValidTokens(Long userId);

	void deleteAll(List<FcmToken> failedList);

	Optional<FcmToken> findByUserIdAndDeviceId(Long userId, String deviceId);

	long deleteToken(Long userId, String deviceId);
}
