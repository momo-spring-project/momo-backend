package com.example.momo.domain.messagehub.application.handler;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.dto.MessageDto;
import com.example.momo.domain.messagehub.application.provider.MessageProvider;
import com.example.momo.domain.messagehub.event.rabbitmq.producer.NotificationMessageProducer;
import com.example.momo.global.rabbitmq.dto.follow.FollowAlarmMessages;
import com.example.momo.global.rabbitmq.dto.meeting.MeetingAlarmMessages;
import com.example.momo.global.rabbitmq.dto.messagehub.DomainAlarmMessage;
import com.example.momo.global.rabbitmq.dto.payment.PaymentAlarmMessages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 도메인 이벤트를 수신하여 알림 이벤트로 변환하고 발행하는 라우팅 핸들러입니다.
 * <p>
 * 다양한 도메인 이벤트를 받아
 * {@link MessageProvider}를 통해 {@link MessageDto}로 변환하고,
 * Spring 의 {@link ApplicationEventPublisher}를 통해 알림 이벤트를 발행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventRoutingHandler {

	private final MessageProvider messageProvider;

	private final NotificationMessageProducer messagePublisher;

	public void handleMessage(DomainAlarmMessage event) {
		MessageDto dto = createMessageDto(event);

		if (dto == null) {
			log.warn("MessageEvent 발행 시도 - 이벤트가 null입니다");
			return;
		}
		publishMessageEvent(dto);
	}

	private MessageDto createMessageDto(DomainAlarmMessage event) {
		if (event instanceof MeetingAlarmMessages.MeetingAlarmMessage e) {
			return messageProvider.processMeetingMessage(e);
		} else if (event instanceof PaymentAlarmMessages.PaymentAlarmMessage e) {
			return messageProvider.processPaymentMessage(e);
		} else if (event instanceof FollowAlarmMessages.FollowAlarmMessage e) {
			return messageProvider.processFollowMessage(e);
		} else {
			return null;
		}
	}

	private void publishMessageEvent(MessageDto messageDto) {
		int maxAttempts = 3;
		for (Long userId : messageDto.userIdList()) {
			publishWithRetry(messageDto, userId, maxAttempts);
		}
	}

	private void publishWithRetry(MessageDto message, Long userId, int maxAttempts) {
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			try {
				messagePublisher.publish(message.toMessage(userId));
				return;
			} catch (Exception e) {
				if (attempt == maxAttempts) {
					log.error("[알림 발행 실패] {}회 시도 - userId={}, targetId={}",
						attempt,
						userId,    // Message에 getter 있다고 가정
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
