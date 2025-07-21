package com.example.momo.domain.notification.infra;

import java.util.List;

import com.example.momo.domain.notification.domain.Notification;
import com.example.momo.domain.notification.domain.NotificationResponse;

public interface NotificationRepository {
	void save(Notification notification);

	List<NotificationResponse> findAllByUserId(Long userId);
}
