package com.example.momo.domain.notification.infra;

import java.util.List;

import com.example.momo.domain.notification.domain.Notification;

public interface NotificationRepository {
	void save(Notification notification);

	List<Notification> findAllByUserId(Long userId);
}
