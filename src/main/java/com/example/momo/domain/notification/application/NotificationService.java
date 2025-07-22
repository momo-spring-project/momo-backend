package com.example.momo.domain.notification.application;

import java.util.List;

import com.example.momo.domain.notification.domain.dto.NotificationResponse;
import com.example.momo.domain.notification.domain.dto.NotificationMeetingEvent;

public interface NotificationService {
	/**
	 * 알림 정보를 데이터베이스에 저장합니다.
	 *
	 * @param command 저장할 알림 정보를 담은 객체
	 */
	void saveNotification(NotificationMeetingEvent command);

	/**
	 * 특정 사용자의 알림 목록을 조회합니다.
	 *
	 * @param userId 알림을 조회할 사용자의 ID
	 * @return 해당 사용자의 알림 목록
	 */
	List<NotificationResponse> getNotifications(Long userId);

	/**
	 * 사용자에게 실시간 알림 메시지를 전송하는 로직을 불러옵니다.
	 *
	 * @param command 전송할 알림 정보를 담은 객체
	 */
	void sendNotification(NotificationMeetingEvent command);
}
