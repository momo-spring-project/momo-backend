package com.example.momo.domain.user.application;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.user.domain.UserOutboxEvent;
import com.example.momo.domain.user.domain.UserOutboxEventRepository;
import com.example.momo.domain.user.event.rabbitmq.producer.UserEventPublisher;
import com.example.momo.domain.user.exception.UserErrorCode;
import com.example.momo.domain.user.exception.UserException;
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

		} catch (DataAccessException e) {
			log.error("아웃박스 이벤트 DB 저장 실패: userId={}, error={}", userId, e.getMessage(), e);
			throw new UserException(UserErrorCode.OUTBOX_EVENT_SAVE_FAILED);
		} catch (IllegalArgumentException e) {
			log.error("아웃박스 이벤트 생성 실패 - 잘못된 파라미터: userId={}, error={}", userId, e.getMessage(), e);
			throw new UserException(UserErrorCode.OUTBOX_EVENT_INVALID_PARAMETER);
		} catch (Exception e) {
			log.error("아웃박스 이벤트 처리 중 예상치 못한 오류: {}", e.getMessage(), e);
			throw new RuntimeException("아웃박스 이벤트 처리 중 예상치 못한 오류가 발생했습니다", e);
		}
	}

	@Override
	@Transactional
	public void markEventAsPublished(Long userId, String eventType) {
		try {
			int updatedCount = outboxEventRepository.markAsPublished(userId, eventType);

			if (updatedCount == 0) {
				log.warn("발행 완료 처리할 아웃박스 이벤트를 찾을 수 없음: userId={}, eventType={}", userId, eventType);
			} else {
				log.info("아웃박스 이벤트 발행 완료 처리: userId={}, eventType={}, updatedCount={}",
					userId, eventType, updatedCount);
			}

		} catch (DataAccessException e) {
			log.error("아웃박스 이벤트 발행 완료 처리 중 DB 오류: userId={}, eventType={}, error={}",
				userId, eventType, e.getMessage(), e);
			throw new UserException(UserErrorCode.OUTBOX_EVENT_PUBLISH_FAILED);
		} catch (Exception e) {
			log.error("아웃박스 이벤트 처리 중 예상치 못한 오류: {}", e.getMessage(), e);
			throw new RuntimeException("아웃박스 이벤트 처리 중 예상치 못한 오류가 발생했습니다", e);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserOutboxEvent> getRetryableEvents(int maxRetryCount) {
		try {
			return outboxEventRepository.findUnpublishedEventsWithRetryCountLessThan(maxRetryCount);
		} catch (DataAccessException e) {
			log.error("재시도 가능한 아웃박스 이벤트 조회 중 DB 오류: maxRetryCount={}, error={}",
				maxRetryCount, e.getMessage(), e);
			throw new UserException(UserErrorCode.OUTBOX_EVENT_RETRY_FAILED);
		} catch (Exception e) {
			log.error("실패한 아웃박스 이벤트 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
			throw new RuntimeException("아웃박스 이벤트 처리 중 예상치 못한 오류가 발생했습니다", e);
		}
	}

	@Override
	@Transactional
	public void retryEvent(UserOutboxEvent outboxEvent) {
		try {
			log.info("아웃박스 이벤트 재시도: id={}, userId={}, eventType={}",
				outboxEvent.getId(), outboxEvent.getUserId(), outboxEvent.getEventType());

			if ("USER_WITHDRAWN".equals(outboxEvent.getEventType())) {
				retryUserWithdrawnEvent(outboxEvent);
			} else {
				log.warn("지원하지 않는 이벤트 타입: {}", outboxEvent.getEventType());
				throw new UserException(UserErrorCode.OUTBOX_EVENT_UNSUPPORTED_TYPE);
			}

			markEventAsPublished(outboxEvent.getUserId(), outboxEvent.getEventType());

		} catch (UserException e) {
			incrementRetryCountSafely(outboxEvent.getId());
			throw e;
		} catch (Exception e) {
			log.error("아웃박스 이벤트 재시도 중 예상치 못한 오류: {}", e.getMessage(), e);
			throw new RuntimeException("아웃박스 이벤트 처리 중 예상치 못한 오류가 발생했습니다", e);
		}
	}

	@Override
	@Transactional
	public int cleanupOldPublishedEvents(int daysOld) {
		try {
			if (daysOld < 1) {
				throw new UserException(UserErrorCode.OUTBOX_EVENT_INVALID_PARAMETER);
			}

			log.info("{}일 이전 발행 완료된 아웃박스 이벤트 정리 시작", daysOld);

			int deletedCount = outboxEventRepository.deleteOldPublishedEvents(daysOld);

			log.info("{}일 이전 발행 완료된 아웃박스 이벤트 {}개 삭제 완료", daysOld, deletedCount);

			return deletedCount;

		} catch (UserException e) {
			// UserException은 그대로 재던짐
			throw e;
		} catch (DataAccessException e) {
			log.error("아웃박스 이벤트 정리 중 DB 오류: daysOld={}, error={}",
				daysOld, e.getMessage(), e);
			throw new UserException(UserErrorCode.OUTBOX_EVENT_CLEANUP_FAILED);
		} catch (Exception e) {
			log.error("아웃박스 이벤트 정리 중 예상치 못한 오류: {}", e.getMessage(), e);
			throw new RuntimeException("아웃박스 이벤트 처리 중 예상치 못한 오류가 발생했습니다", e);
		}
	}

	private void retryUserWithdrawnEvent(UserOutboxEvent outboxEvent) {
		try {
			JsonNode eventData = objectMapper.readTree(outboxEvent.getEventData());

			JsonNode userIdNode = eventData.get("userId");
			JsonNode emailNode = eventData.get("email");
			JsonNode nicknameNode = eventData.get("nickname");

			if (userIdNode == null || emailNode == null || nicknameNode == null) {
				throw new UserException(UserErrorCode.OUTBOX_EVENT_DATA_PARSING_FAILED);
			}

			Long userId = userIdNode.asLong();
			String email = emailNode.asText();
			String nickname = nicknameNode.asText();

			userEventPublisher.publishUserWithdrawn(userId, email, nickname);
			log.info("회원탈퇴 이벤트 재시도 성공: userId={}", userId);

		} catch (UserException e) {
			// UserException은 그대로 재던짐
			throw e;
		} catch (JsonProcessingException e) {
			log.error("아웃박스 이벤트 데이터 파싱 실패: eventData={}, error={}",
				outboxEvent.getEventData(), e.getMessage(), e);
			throw new UserException(UserErrorCode.OUTBOX_EVENT_DATA_PARSING_FAILED);
		} catch (Exception e) {
			log.error("RabbitMQ 메시지 발행 실패: userId={}, error={}",
				outboxEvent.getUserId(), e.getMessage(), e);
			throw new UserException(UserErrorCode.OUTBOX_EVENT_PUBLISH_FAILED);
		}
	}

	private void incrementRetryCountSafely(Long outboxEventId) {
		try {
			outboxEventRepository.incrementRetryCount(outboxEventId);
		} catch (Exception e) {
			log.error("재시도 횟수 증가 실패: outboxEventId={}, error={}",
				outboxEventId, e.getMessage(), e);
		}
	}
}