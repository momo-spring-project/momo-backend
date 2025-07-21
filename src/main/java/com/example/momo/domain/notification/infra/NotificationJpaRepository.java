package com.example.momo.domain.notification.infra;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.momo.domain.notification.domain.Notification;
import com.example.momo.domain.notification.domain.NotificationResponse;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {
	@Query("""
		SELECT new com.example.momo.domain.notification.domain.NotificationResponse(
		    n.id, n.meetingId, n.content, n.createdAt
		)
		FROM Notification n
		WHERE n.userId = :userId
		""")
	List<NotificationResponse> findAllByUserId(@Param("userId") Long userId);
}
