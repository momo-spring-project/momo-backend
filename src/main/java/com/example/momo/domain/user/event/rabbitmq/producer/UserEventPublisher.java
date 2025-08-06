package com.example.momo.domain.user.event.rabbitmq.producer;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;
import com.example.momo.global.rabbitmq.constant.RoutingKeys;
import com.example.momo.global.rabbitmq.dto.UserEventMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserEventPublisher {

	private final RabbitTemplate userRabbitTemplate;

	// 수동으로 생성자 작성 - @Qualifier가 제대로 동작함
	public UserEventPublisher(@Qualifier("userRabbitTemplate") RabbitTemplate userRabbitTemplate) {
		this.userRabbitTemplate = userRabbitTemplate;
	}

	/**
	 * 사용자 탈퇴 이벤트 발행
	 *
	 * @param userId 탈퇴한 사용자 ID
	 * @param email 사용자 이메일
	 * @param nickname 사용자 닉네임
	 */
	public void publishUserWithdrawn(Long userId, String email, String nickname) {
		UserEventMessage message = UserEventMessage.createWithdrawn(userId, email, nickname);
		String correlationId = "user-withdrawn-" + userId;

		publishEvent(message, RoutingKeys.USER_WITHDRAWN, correlationId);
	}

	/**
	 * User 이벤트 메시지 발행 (공통 로직)
	 *
	 * @param message 발행할 메시지
	 * @param routingKey 라우팅 키
	 * @param correlationId 상관관계 ID (Publisher Confirm용)
	 */
	private void publishEvent(UserEventMessage message, String routingKey, String correlationId) {
		try {
			// Publisher Confirm을 위한 CorrelationData 생성
			CorrelationData correlationData = new CorrelationData(correlationId);

			// 메시지 발행 - userRabbitTemplate에 설정된 Publisher Confirm, PERSISTENT 등이 자동 적용됨
			userRabbitTemplate.convertAndSend(
				RabbitExchangeNames.USER_EVENTS,  // Exchange: "momo.user.events"
				routingKey,                       // RoutingKey: "user.withdrawn"
				message,                          // 실제 메시지 내용
				correlationData                   // 발행 결과 추적용 ID
			);

			log.info("[User] 이벤트 발행 요청 완료 - eventType: {}, correlationId: {}",
				message.eventType(), correlationId);

		} catch (Exception e) {
			log.error("[User] 이벤트 발행 실패 - eventType: {}, correlationId: {}, error: {}",
				message.eventType(), correlationId, e.getMessage(), e);
			throw new RuntimeException("RabbitMQ 메시지 발행 실패", e);
		}
	}
}