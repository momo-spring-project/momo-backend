package com.example.momo.domain.notification.infra;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.momo.domain.notification.domain.Notification;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {
	List<Notification> findAllByUserId(Long userId);
}
