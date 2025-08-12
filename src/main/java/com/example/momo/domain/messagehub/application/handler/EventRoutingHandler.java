package com.example.momo.domain.messagehub.application.handler;

import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.dto.MessageDto;
import com.example.momo.domain.messagehub.application.provider.MessageProvider;
import com.example.momo.domain.messagehub.application.service.MessageHubRedisService;
import com.example.momo.domain.messagehub.event.rabbitmq.producer.MessageHubProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 도메인 이벤트를 수신하여 알림 이벤트로 변환하고 발행하는 라우팅 핸들러입니다.
 * <p>
 * 다양한 도메인 이벤트를 받아
 * {@link MessageProvider}를 통해 {@link MessageDto}로 변환하고,
 * Spring 의 {@link MessageHubProducer}를 통해 알림 이벤트를 발행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventRoutingHandler {

	private final MessageProvider messageProvider;

	private final MessageHubProducer messagePublisher;

	private final MessageHubRedisService messageHubRedisService;

	// 전체 로직 흐름 처리
	public void handleMessage(String uuid, String type, Object object) {
		//uuid 중복 확인
		if (messageHubRedisService.isUuidExistOrSave(uuid)) {
			return;
		}

		MessageDto dto = createMessageDto(type, object);

		if (dto == null) {
			log.warn("MessageEvent 발행 실패");
			return;
		}
		publishMessageEvent(dto);
	}

	// 각 도메인으로 1차 구분
	private MessageDto createMessageDto(String type, Object object) {
		String domain = extractDomain(type).toLowerCase();
		try {
			switch (domain) {
				case "meeting" -> {
					return messageProvider.processMeetingMessage(type, object);
				}
				case "payment" -> {
					return messageProvider.processPaymentMessage(type, object);
				}
				case "follow" -> {
					return messageProvider.processFollowMessage(type, object);
				}
				default -> {
					log.warn("지원하지 않는 이벤트 타입: type={}", type);
					return null;
				}
			}
		} catch (IllegalArgumentException e) {
			log.error("알림 타입 불일치 : type={}", type);
			return null;
		} catch (Exception e) {
			log.error("알 수 없는 예외 발생 : type={}", type);
			return null;
		}

	}

	// 메세지에서 도메인 타입 추출
	private String extractDomain(String type) {
		int idx = type.indexOf('.');
		return (idx > 0 ? type.substring(0, idx) : type).toLowerCase();
	}

	// 메세지 이벤트 발행
	private void publishMessageEvent(MessageDto messageDto) {
		int maxAttempts = 3;
		for (Long userId : messageDto.userIdList()) {
			publishWithRetry(messageDto, userId, maxAttempts);
		}
	}

	// 메세지 발행 실패시 최대 3회까지 시도 후 error 로그 생성
	private void publishWithRetry(MessageDto message, Long userId, int maxAttempts) {
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			try {
				messagePublisher.publish(message.toMessage(userId));
				return;
			} catch (Exception e) {
				if (attempt == maxAttempts) {
					log.error("[알림 발행 실패] {}회 시도 - userId={}, targetId={}",
						attempt,
						userId,
						message.targetId());
				} else {
					try {
						Thread.sleep(100);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}

}
