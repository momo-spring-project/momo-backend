package com.example.momo.domain.notification.application;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.dto.MessageDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 알림 관련 도메인 이벤트를 비동기적으로 처리하는 이벤트 리스너입니다.
 *
 * <p>
 * NotificationService 를 통해 알림을 DB에 저장하고, WebSocket 으로 전송하는 역할을 합니다.
 * 이벤트는 트랜잭션 커밋 이후에 비동기로 처리되며, 유효성 검사를 통과하지 못할 경우 무시됩니다.
 * 주의: 필수 필드가 누락된 이벤트는 처리되지 않으며, 로그만 출력됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

	private final NotificationHandler notificationHandler;

	/**
	 * {@link MessageDto} 이벤트를 처리합니다.
	 * 필수 정보가 모두 포함된 경우에만 알림을 저장하고 WebSocket 으로 전송합니다.
	 *
	 * @param event 알림 전송에 필요한 이벤트 데이터
	 */
	@EventListener
	public void handleMessageEvent(MessageDto event) {
		log.warn("현재 사용되지 않는 이벤트 - RabbitListener 로 변경 필요");

	}
}
