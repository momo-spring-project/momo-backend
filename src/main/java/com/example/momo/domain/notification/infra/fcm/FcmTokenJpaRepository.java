package com.example.momo.domain.notification.infra.fcm;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.momo.domain.notification.domain.FcmToken;

public interface FcmTokenJpaRepository extends JpaRepository<FcmToken, Long> {
	List<FcmToken> findAllByUserId(Long userId);
}
