package com.example.momo.domain.notification.service;

import com.example.momo.domain.notification.dto.meeting.NotificationMeetingEvent;

public interface NotificationService {
	/**
	 * 알림을 저장하고 실시간으로 사용자에게 전송하는 로직을 담고 있습니다.
	 *
	 * @param command 알림 처리에 필요한 정보(command 객체)
	 */
	void processNotification(NotificationMeetingEvent command);

	/**
	 * 알림 정보를 데이터베이스에 저장합니다.
	 *
	 * @param command 저장할 알림 정보를 담은 객체
	 */
	void saveNotification(NotificationMeetingEvent command);

	/**
	 * WebSocket 을 통해 사용자에게 실시간으로 알림 메시지를 전송합니다.
	 *
	 * @param command 메시지 전송에 필요한 정보를 담은 객체
	 */
	void sendNotification(NotificationMeetingEvent command);
}
