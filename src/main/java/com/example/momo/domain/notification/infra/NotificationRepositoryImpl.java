package com.example.momo.domain.notification.infra;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.momo.domain.notification.domain.Notification;
import com.example.momo.domain.notification.domain.NotificationResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

	private final NotificationJpaRepository notificationJpaRepository;

	@Override
	public Notification save(Notification notification) {
		return notificationJpaRepository.save(notification);
	}

	@Override
	public List<NotificationResponse> findAllByUserId(Long userId) {
		return notificationJpaRepository.findAllByUserId(userId);
	}
}
