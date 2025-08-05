package com.example.momo.domain.user.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.user.domain.UserOutboxEvent;
import com.example.momo.domain.user.domain.UserOutboxEventRepository;
import com.example.momo.domain.user.event.rabbitmq.producer.UserEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
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

	private final UserOutboxEventRepository outboxEventRepository;
	private final UserEventPublisher userEventPublisher;
	private final ObjectMapper objectMapper;

	@Override
	@Transactional
	public void saveUserWithdrawnEvent(Long userId, String email, String nickname) {
		try {
			// JSON 형태로 이벤트 데이터 저장
			String eventData = String.format(
				"{\"userId\":%d,\"email\":\"%s\",\"nickname\":\"%s\"}",
				userId, email, nickname
			);

			UserOutboxEvent outboxEvent = UserOutboxEvent.create(
				userId,
				"USER_WITHDRAWN",
				eventData
			);

			outboxEventRepository.save(outboxEvent);

			log.info("회원탈퇴 아웃박스 이벤트 저장 완료: userId={}", userId);

		} catch (Exception e) {
			log.error("회원탈퇴 아웃박스 이벤트 저장 실패: userId={}, error={}",
				userId, e.getMessage(), e);
			throw e;
		}
	}

	@Override
	@Transactional
	public void markEventAsPublished(Long userId, String eventType) {
		try {
			outboxEventRepository.markAsPublished(userId, eventType);
			log.info("아웃박스 이벤트 발행 완료 처리: userId={}, eventType={}", userId, eventType);

		} catch (Exception e) {
			log.error("아웃박스 이벤트 발행 완료 처리 실패: userId={}, eventType={}, error={}",
				userId, eventType, e.getMessage(), e);
			throw e;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserOutboxEvent> getUnpublishedEvents() {
		return outboxEventRepository.findUnpublishedEvents();
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserOutboxEvent> getRetryableEvents(int maxRetryCount) {
		return outboxEventRepository.findUnpublishedEventsWithRetryCountLessThan(maxRetryCount);
	}

	@Override
	@Transactional
	public void retryEvent(UserOutboxEvent outboxEvent) {
		try {
			log.info("아웃박스 이벤트 재시도: id={}, userId={}, eventType={}",
				outboxEvent.getId(), outboxEvent.getUserId(), outboxEvent.getEventType());

			// 이벤트 타입에 따라 적절한 RabbitMQ 메시지 발행
			if ("USER_WITHDRAWN".equals(outboxEvent.getEventType())) {
				retryUserWithdrawnEvent(outboxEvent);
			}

			// 재시도 성공 시 발행 완료 처리
			markEventAsPublished(outboxEvent.getUserId(), outboxEvent.getEventType());

		} catch (Exception e) {
			log.error("아웃박스 이벤트 재시도 실패: id={}, error={}",
				outboxEvent.getId(), e.getMessage(), e);

			// 재시도 횟수 증가
			outboxEventRepository.incrementRetryCount(outboxEvent.getId());
			throw e;
		}
	}

	@Override
	@Transactional
	public int cleanupOldPublishedEvents(int daysOld) {
		log.info("{}일 이전 발행 완료된 아웃박스 이벤트 정리 시작", daysOld);

		int deletedCount = outboxEventRepository.deleteOldPublishedEvents(daysOld);

		log.info("{}일 이전 발행 완료된 아웃박스 이벤트 {}개 삭제 완료", daysOld, deletedCount);

		return deletedCount;
	}

	/**
	 * 회원탈퇴 이벤트 재시도 처리
	 */
	private void retryUserWithdrawnEvent(UserOutboxEvent outboxEvent) {
		try {
			// JSON 데이터 파싱
			JsonNode eventData = objectMapper.readTree(outboxEvent.getEventData());
			Long userId = eventData.get("userId").asLong();
			String email = eventData.get("email").asText();
			String nickname = eventData.get("nickname").asText();

			// RabbitMQ 메시지 재발행
			userEventPublisher.publishUserWithdrawn(userId, email, nickname);

			log.info("회원탈퇴 이벤트 재시도 성공: userId={}", userId);

		} catch (JsonProcessingException e) {
			log.error("아웃박스 이벤트 데이터 파싱 실패: {}", e.getMessage(), e);
			throw new RuntimeException("이벤트 데이터 파싱 실패", e);
		}
	}
}