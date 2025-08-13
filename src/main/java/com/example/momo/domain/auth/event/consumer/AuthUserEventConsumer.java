package com.example.momo.domain.auth.event.consumer;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
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
import com.fasterxml.jackson.databind.ObjectMapper;

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
	private final ObjectMapper objectMapper;

	@Retryable(
		retryFor = {RuntimeException.class},
		maxAttempts = 3,
		backoff = @Backoff(delay = 500, multiplier = 1.5)
	)
	@RabbitHandler
	@Transactional
	public void handleUserEvent(EventWrapper<?> eventWrapper,
		@Header(value = "correlationId", required = false) String correlationId) {

		// correlationId가 없으면 기본값 생성
		if (correlationId == null) {
			correlationId = "unknown-" + eventWrapper.uuId();
		}

		// 1. EventWrapper 기본 검증
		if (eventWrapper == null) {
			log.error("[{}] EventWrapper가 null입니다", correlationId);
			throw new IllegalArgumentException("EventWrapper cannot be null");
		}

		// 2. type 검증
		String eventType = eventWrapper.type();
		if (eventType == null || eventType.trim().isEmpty()) {
			log.error("[{}] 이벤트 타입이 null이거나 비어있습니다: eventId={}",
				correlationId, eventWrapper.uuId());
			throw new IllegalArgumentException("Event type cannot be null or empty");
		}

		// 3. data 검증
		if (eventWrapper.data() == null) {
			log.error("[{}] 이벤트 데이터가 null입니다: eventType={}, eventId={}",
				correlationId, eventType, eventWrapper.uuId());
			throw new IllegalArgumentException("Event data cannot be null");
		}

		log.info("[{}] 이벤트 처리 시작: eventType={}, eventId={}",
			correlationId, eventType, eventWrapper.uuId());

		switch (eventType) {
			case "user.withdrawn" -> handleUserWithdrawn(eventWrapper, correlationId);
			default -> {
				log.error("[{}] 지원하지 않는 이벤트 타입: {}", correlationId, eventType);
				throw new UnsupportedOperationException("지원하지 않는 이벤트 타입: " + eventType);
			}
		}
	}

	/**
	 * 메시지 소비를 3번 시도하고 실패하면 DLQ로 이동
	 */
	@Recover
	public void recover(Exception e, EventWrapper<?> eventWrapper, String correlationId) {
		// correlationId가 없으면 기본값
		if (correlationId == null) {
			correlationId = "unknown-" + eventWrapper.uuId();
		}

		log.error("[{}] User 이벤트 처리 실패: eventType={}, eventId={}, error={}",
			correlationId, eventWrapper.type(), eventWrapper.uuId(), e.getMessage(), e);

		// Slack 알림에도 correlationId 포함
		slackNotifier.notifyMessageConsumeFailure(
			this.getClass().getSimpleName(),
			eventWrapper.type() + " (correlationId: " + correlationId + ")",
			AuthRabbitMQConfig.AUTH_USER_EVENTS_QUEUE,
			e
		);

		throw new AmqpRejectAndDontRequeueException(
			"Retry 실패, DLQ로 이동: eventType=" + eventWrapper.type() + ", correlationId=" + correlationId, e);
	}

	/**
	 * 회원탈퇴 이벤트 처리
	 */
	private void handleUserWithdrawn(EventWrapper<?> eventWrapper, String correlationId) {
		try {
			// ObjectMapper를 사용해서 LinkedHashMap -> UserWithdrawnData 변환
			UserEventMessage.UserWithdrawnData data = objectMapper.convertValue(
				eventWrapper.data(),
				UserEventMessage.UserWithdrawnData.class
			);

			// 필수 필드 검증
			validateUserWithdrawnData(data, correlationId);

			log.info("[{}] 회원탈퇴 이벤트 수신: userId={}, email={}, eventId={}",
				correlationId, data.userId(), data.email(), eventWrapper.uuId());

			// 비즈니스 로직 실행
			userSocialRepository.deleteAllByUserId(data.userId());

			log.info("[{}] 계정({})에 연동된 소셜 로그인을 모두 삭제했습니다.",
				correlationId, data.email());

		} catch (Exception e) {
			log.error("[{}] 소셜 로그인 삭제 실패: error={}", correlationId, e.getMessage());
			throw e;
		}
	}

	/**
	 * UserWithdrawnData 필수 필드 검증
	 */
	private void validateUserWithdrawnData(UserEventMessage.UserWithdrawnData data, String correlationId) {
		if (data.userId() == null) {
			log.error("[{}] userId가 null입니다", correlationId);
			throw new IllegalArgumentException("userId는 필수입니다");
		}

		if (data.userId() <= 0) {
			log.error("[{}] 잘못된 userId: {}", correlationId, data.userId());
			throw new IllegalArgumentException("userId는 양수여야 합니다");
		}

		if (data.email() == null || data.email().trim().isEmpty()) {
			log.error("[{}] email이 null이거나 비어있습니다: userId={}", correlationId, data.userId());
			throw new IllegalArgumentException("email은 필수입니다");
		}

		if (data.nickname() == null || data.nickname().trim().isEmpty()) {
			log.error("[{}] nickname이 null이거나 비어있습니다: userId={}", correlationId, data.userId());
			throw new IllegalArgumentException("nickname은 필수입니다");
		}
	}
}
