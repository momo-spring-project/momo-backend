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

import com.example.momo.domain.auth.event.config.AuthRabbitMQConfig;
import com.example.momo.domain.auth.infra.UserSocialRepository;
import com.example.momo.domain.auth.slack.SlackNotifier;
import com.example.momo.global.rabbitmq.dto.User.UserEventMessage;
import com.example.momo.global.rabbitmq.dto.common.EventWrapper;

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
	public void handleUserEvent(EventWrapper<?> eventWrapper) {
		switch (eventWrapper.type()) {
			case "user.withdrawn" -> handleUserWithdrawn(eventWrapper);
			default -> throw new RuntimeException("지원하지 않는 이벤트 타입: " + eventWrapper.type());
		}
	}

	/**
	 * 메시지 소비를 3번 시도하고 실패하면 DLQ로 이동
	 */
	@Recover
	public void recover(Exception e, EventWrapper<?> eventWrapper) {
		// Retryable 의 최대 횟수를 모두 실패하면 error 로그를 남김.
		log.error("User 이벤트 처리 실패: eventType={}, eventId={}, error={}",
			eventWrapper.type(), eventWrapper.uuId(), e.getMessage(), e);

		// Slack 메시지를 보냄
		slackNotifier.notifyMessageConsumeFailure(
			this.getClass().getSimpleName(),
			eventWrapper.type(),
			AuthRabbitMQConfig.AUTH_USER_EVENTS_QUEUE,
			e
		);

		// Slack 메시지를 보내고 DLQ로 이동
		throw new AmqpRejectAndDontRequeueException(
			"Retry 실패, DLQ로 이동: eventType=" + eventWrapper.type(), e);
	}

	/**
	 * 회원탈퇴 이벤트 처리
	 */
	private void handleUserWithdrawn(EventWrapper<?> eventWrapper) {
		// RabbitMQ 에서 자동 역직렬화된 데이터를 직접 캐스팅
		UserEventMessage.UserWithdrawnData data = (UserEventMessage.UserWithdrawnData)eventWrapper.data();

		log.info("회원탈퇴 이벤트 수신: userId={}, email={}, eventId={}", data.userId(), data.email(), eventWrapper.uuId());

		userSocialRepository.deleteAllByUserId(data.userId());

		log.info("계정({})에 연동된 소셜 로그인을 모두 삭제했습니다.", data.email());
	}
}
