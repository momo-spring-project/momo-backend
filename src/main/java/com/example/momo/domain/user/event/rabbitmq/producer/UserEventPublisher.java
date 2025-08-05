package com.example.momo.domain.user.event.rabbitmq.producer;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.momo.domain.user.event.rabbitmq.config.UserRabbitMQConfig;
import com.example.momo.global.rabbitmq.dto.UserEventMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

	private final RabbitTemplate rabbitTemplate;

	public void publishUserWithdrawn(Long userId, String email, String nickname) {
		UserEventMessage message = UserEventMessage.createWithdrawn(userId, email, nickname);
		publishEvent(message, "user.withdrawn");
	}

	public void publishUserRegistered(Long userId, String nickname, String email,
		Double latitude, Double longitude, List<Integer> categoryIds) {
		UserEventMessage message = UserEventMessage.createRegistered(userId, nickname, email, latitude, longitude,
			categoryIds);
		publishEvent(message, "user.registered");
	}

	public void publishUserFollowed(Long followerId, Long followingId,
		String followerNickname, String followingNickname) {
		UserEventMessage message = UserEventMessage.createFollowed(followerId, followingId, followerNickname,
			followingNickname);
		publishEvent(message, "user.followed");
	}

	public void publishUserRated(Long reviewerId, Long targetUserId, Long meetingId,
		Integer ratingScore, String reviewerNickname, String targetUserNickname) {
		UserEventMessage message = UserEventMessage.createRated(reviewerId, targetUserId, meetingId,
			ratingScore, reviewerNickname, targetUserNickname);
		publishEvent(message, "user.rated");
	}

	private void publishEvent(UserEventMessage message, String routingKey) {
		try {
			rabbitTemplate.convertAndSend(UserRabbitMQConfig.USER_EVENTS_EXCHANGE, routingKey, message);
			log.info("User 이벤트 발행 완료: eventType={}", message.eventType());
		} catch (Exception e) {
			log.error("User 이벤트 발행 실패: eventType={}, error={}",
				message.eventType(), e.getMessage(), e);
		}
	}
}
