package com.example.momo.domain.user.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.user.domain.UserOutboxEvent;
import com.example.momo.domain.user.domain.UserOutboxEventRepository;
import com.example.momo.domain.user.event.rabbitmq.producer.UserEventPublisher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 아웃박스 이벤트 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserOutboxServiceImpl implements UserOutboxService {

	private final UserOutboxEventRepository userOutboxEventRepository;
	private final UserEventPublisher userEventPublisher;
	private final ObjectMapper objectMapper;

	@Override
	@Transactional
	public void saveUserWithdrawnEvent(Long userId, String email, String nickname) {
		try {
			String eventData = String.format(
				"{\"userId\":%d,\"email\":\"%s\",\"nickname\":\"%s\"}",
				userId, email, nickname
			);
			UserOutboxEvent outboxEvent = UserOutboxEvent.create(userId, "USER_WITHDRAWN", eventData);
			userOutboxEventRepository.save(outboxEvent);
			log.info("회원탈퇴 아웃박스 이벤트 저장 완료: userId={}", userId);
		} catch (Exception e) {
			log.error("아웃박스 이벤트 저장 실패: {}", e.getMessage(), e);
			throw new RuntimeException("이벤트 저장 실패");
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markEventAsPublished(Long userId, String eventType) {
		try {
			userOutboxEventRepository.markAsPublished(userId, eventType);
		} catch (Exception e) {
			log.error("아웃박스 이벤트 마킹 실패: {}", e.getMessage(), e);
			throw new RuntimeException("마킹 실패");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserOutboxEvent> getUnpublishedEvents() {
		return userOutboxEventRepository.findUnpublishedEvents();
	}

	@Override
	@Transactional
	public void publishEvent(UserOutboxEvent outboxEvent) {
		try {
			JsonNode eventData = objectMapper.readTree(outboxEvent.getEventData());
			Long userId = eventData.get("userId").asLong();
			String email = eventData.get("email").asText();
			String nickname = eventData.get("nickname").asText();

			userEventPublisher.publishUserWithdrawn(userId, email, nickname);

			markEventAsPublished(userId, outboxEvent.getEventType());
		} catch (Exception e) {
			log.error("이벤트 발행 실패: {}", e.getMessage(), e);
		}
	}

	@Override
	@Transactional
	public int cleanupOldPublishedEvents(int daysOld) {
		return userOutboxEventRepository.deleteOldPublishedEvents(daysOld);
	}
}