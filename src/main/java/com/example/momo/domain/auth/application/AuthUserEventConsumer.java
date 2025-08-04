package com.example.momo.domain.auth.application;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.momo.domain.auth.application.dto.event.AuthEventMessage;
import com.example.momo.domain.auth.infra.UserSocialRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = "auth.user.events.queue")
public class AuthUserEventConsumer {

	private final UserSocialRepository userSocialRepository;

	@RabbitHandler
	public void handleUserEvent(AuthEventMessage message) {
		try {
			if (message.eventType().equals("user.withdrawn")) {
				handleUserWithdrawn(message);
				// 필요한 이벤트들 추가 -> 케이스 많아지면 switch 문으로 변경
			} else {
				log.warn("처리되지 않은 이벤트 타입: {}", message.eventType());
			}
		} catch (Exception e) {
			log.error("User 이벤트 처리 실패: eventType={}, error={}",
				message.eventType(), e.getMessage(), e);
			throw e;
		}
	}

	private void handleUserWithdrawn(AuthEventMessage message) {
		AuthEventMessage.UserWithdrawnData data = message.getUserWithdrawnData();

		log.info("회원탈퇴 이벤트 수신: userId={}, email={}", data.userId(), data.email());

		userSocialRepository.deleteAllByUserId(data.userId());

		log.info("계정({})에 연동된 소셜 로그인을 모두 삭제했습니다.", data.email());
	}
}
