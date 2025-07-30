package com.example.momo.domain.notification.domain;

import java.util.List;

public interface FcmTokenRepository {
	void save(FcmToken token);

	List<FcmToken> findValidTokens(Long userId);

	void deleteAll(List<FcmToken> failedList);
}
