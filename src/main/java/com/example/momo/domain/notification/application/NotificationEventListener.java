package com.example.momo.domain.notification.application;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.dto.MessageDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * todo : 삭제예정
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
