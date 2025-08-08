package com.example.momo.domain.auth.event.consumer;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.auth.application.dto.event.AuthEventMessage;
import com.example.momo.domain.auth.event.config.AuthRabbitMQConfig;
import com.example.momo.domain.auth.infra.UserSocialRepository;
import com.example.momo.domain.auth.slack.SlackNotifier;
import com.example.momo.domain.user.domain.User;
import com.example.momo.global.rabbitmq.dto.UserEventMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableRetry
@RabbitListener(queues = AuthRabbitMQConfig.AUTH_USER_EVENTS_QUEUE)
public class AuthUserEventConsumer {
	private final SlackNotifier slackNotifier;
	private final UserSocialRepository userSocialRepository;

	@Retryable(
		retryFor = {RuntimeException.class},
		maxAttempts = 3,
		backoff = @Backoff(delay = 500, multiplier = 1.5)
	)
	@RabbitHandler
	@Transactional
	public void handleUserEvent(UserEventMessage message) {
		if (message.eventType().equals("user.withdrawn")) {
			handleUserWithdrawn(message);
			return;
			// 필요한 이벤트들 추가 -> 케이스 많아지면 switch 문으로 변경
		}

		throw new RuntimeException("지원하지 않는 이벤트 타입:" + message.eventType());
	}

	/**
	 * 메시지 소비를 3번 시도하고 실패하면 DLQ로 이동
	 */
	@Recover
	public void recover(Exception e, UserEventMessage message) {
		// Retryable 의 최대 횟수를 모두 실패하면 error 로그를 남김.
		log.error("User 이벤트 처리 실패: eventType={}, error={}", message.eventType(), e.getMessage(), e);

		// Slack 메시지를 보냄
		slackNotifier.notifyMessageConsumeFailure(this.getClass().getSimpleName(), message.eventType(),
			AuthRabbitMQConfig.AUTH_USER_EVENTS_QUEUE,  e);
		// Slack 메시지를 보내고 DLQ로 이동
		throw new AmqpRejectAndDontRequeueException("Retry 실패, DLQ로 이동: eventType=" + message.eventType(), e);
	}

	private void handleUserWithdrawn(UserEventMessage message) {
		UserEventMessage.UserWithdrawnData data = (UserEventMessage.UserWithdrawnData)message.data();

		log.info("회원탈퇴 이벤트 수신: userId={}, email={}", data.userId(), data.email());

		userSocialRepository.deleteAllByUserId(data.userId());

		log.info("계정({})에 연동된 소셜 로그인을 모두 삭제했습니다.", data.email());
	}
}
