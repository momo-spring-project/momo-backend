package com.example.momo.domain.notification.infra.fcm;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.momo.domain.notification.domain.FcmToken;
import com.example.momo.domain.notification.domain.FcmTokenRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FcmTokenRepositoryImpl implements FcmTokenRepository {

	private final FcmTokenJpaRepository fcmTokenJpaRepository;

	@Override
	public void save(FcmToken token) {
		fcmTokenJpaRepository.save(token);
	}

	@Override
	public List<FcmToken> findValidTokens(Long userId) {

		return fcmTokenJpaRepository.findAllByUserId(userId);
	}

	@Override
	public void deleteAll(List<FcmToken> failedList) {
		fcmTokenJpaRepository.deleteAll(failedList);
	}
}
