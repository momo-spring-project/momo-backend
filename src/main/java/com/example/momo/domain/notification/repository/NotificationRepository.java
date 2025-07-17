package com.example.momo.domain.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.momo.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
