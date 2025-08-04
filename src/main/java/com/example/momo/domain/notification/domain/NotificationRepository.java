package com.example.momo.domain.notification.domain;

import java.util.List;

public interface NotificationRepository {
	Notification save(Notification notification);

	List<Notification> findAllByUserId(Long userId);
}