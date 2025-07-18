package com.example.momo.global.socket.service;

import com.example.momo.global.socket.dto.NotificationMessage;

public interface NotificationSender {

	/**
	 * WebSocket 을 통해 사용자에게 실시간으로 알림 메시지를 전송합니다.
	 *
	 * @param message 메시지 전송에 필요한 정보를 담은 객체
	 */
	void send(NotificationMessage message);
}
