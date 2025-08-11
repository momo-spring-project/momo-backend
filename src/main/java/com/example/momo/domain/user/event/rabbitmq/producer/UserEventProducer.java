package com.example.momo.domain.user.event.rabbitmq.producer;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.example.momo.global.rabbitmq.constant.EventTypeNames;
import com.example.momo.global.rabbitmq.constant.RabbitExchangeNames;
import com.example.momo.global.rabbitmq.constant.RoutingKeys;
import com.example.momo.global.rabbitmq.dto.UserEventMessage;
import com.example.momo.global.rabbitmq.dto.common.EventWrapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserEventProducer {

	private final RabbitTemplate userRabbitTemplate;

	// 수동으로 생성자 작성 - @Qualifier가 제대로 동작함
	public UserEventProducer(@Qualifier("userRabbitTemplate") RabbitTemplate userRabbitTemplate) {
		this.userRabbitTemplate = userRabbitTemplate;
	}

	/**
	 * 사용자 탈퇴 이벤트 발행 (EventWrapper 양식)
	 *
	 * @param userId 탈퇴한 사용자 ID
	 * @param email 사용자 이메일
	 * @param nickname 사용자 닉네임
	 */
	public void publishUserWithdrawn(Long userId, String email, String nickname) {
		UserEventMessage.UserWithdrawnData data = new UserEventMessage.UserWithdrawnData(userId, email, nickname);

		EventWrapper<UserEventMessage.UserWithdrawnData> eventWrapper = EventWrapper.of(
			EventTypeNames.USER_WITHDRAWN,
			data
		);

		String correlationId = "user-withdrawn-" + userId;

		publishEvent(eventWrapper, RoutingKeys.USER_WITHDRAWN, correlationId);
	}

	/**
	 * 사용자 팔로우 이벤트 발행 (EventWrapper 양식)
	 *
	 * @param followerId 팔로우한 사용자 ID
	 * @param followingId 팔로우 당한 사용자 ID
	 * @param followerNickname 팔로우한 사용자 닉네임
	 */
	public void publishUserFollowed(Long followerId, Long followingId, String followerNickname) {
		UserEventMessage.UserFollowedData data =
			new UserEventMessage.UserFollowedData(followerId, followingId,
				followerNickname);

		EventWrapper<UserEventMessage.UserFollowedData> eventWrapper = EventWrapper.of(
			EventTypeNames.USER_FOLLOWED,
			data
		);

		String correlationId = "user-followed-" + followerId + "-" + followingId;

		publishEvent(eventWrapper, RoutingKeys.USER_FOLLOWED, correlationId);
	}

	/**
	 * User 이벤트 메시지 발행 (공통 로직)
	 *
	 * @param eventWrapper 발행할 EventWrapper 메시지
	 * @param routingKey 라우팅 키
	 * @param correlationId 상관관계 ID (Publisher Confirm용)
	 */
	private void publishEvent(EventWrapper<?> eventWrapper, String routingKey, String correlationId) {
		try {
			// Publisher Confirm을 위한 CorrelationData 생성
			CorrelationData correlationData = new CorrelationData(correlationId);

			// 메시지 발행 - userRabbitTemplate에 설정된 Publisher Confirm, PERSISTENT 등이 자동 적용됨
			userRabbitTemplate.convertAndSend(
				RabbitExchangeNames.USER_EVENTS,  // Exchange: "momo.user.events"
				routingKey,                       // RoutingKey: "user.withdrawn"
				eventWrapper,
				correlationData                   // 발행 결과 추적용 ID
			);

			log.info("[User] 이벤트 발행 요청 완료 - eventType: {}, eventId: {}, correlationId: {}",
				eventWrapper.type(), eventWrapper.uuId(), correlationId);

		} catch (Exception e) {
			log.error("[User] 이벤트 발행 실패 - eventType: {}, eventId: {}, correlationId: {}, error: {}",
				eventWrapper.type(), eventWrapper.uuId(), correlationId, e.getMessage(), e);
		}
	}
}