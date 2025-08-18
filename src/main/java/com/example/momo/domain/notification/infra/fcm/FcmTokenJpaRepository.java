package com.example.momo.domain.notification.infra.fcm;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.momo.domain.notification.domain.FcmToken;

public interface FcmTokenJpaRepository extends JpaRepository<FcmToken, Long> {
	List<FcmToken> findAllByUserId(Long userId);

	Optional<FcmToken> findByUserIdAndDeviceId(Long userId, String deviceId);

	long deleteByUserIdAndDeviceId(Long userId, String deviceId);
}
