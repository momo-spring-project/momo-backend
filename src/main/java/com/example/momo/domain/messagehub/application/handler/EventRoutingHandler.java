package com.example.momo.domain.messagehub.application.handler;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.momo.domain.messagehub.application.dto.MessageDto;
import com.example.momo.domain.messagehub.application.provider.MessageProvider;
import com.example.momo.domain.messagehub.event.rabbitmq.producer.NotificationMessageProducer;
import com.example.momo.global.rabbitMQ.dto.follow.FollowMessageEvents;
import com.example.momo.global.rabbitMQ.dto.meeting.MeetingMessageEvents;
import com.example.momo.global.rabbitMQ.dto.messagehub.DomainMessageEvent;
import com.example.momo.global.rabbitMQ.dto.payment.PaymentMessageEvents;

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

	public void handleMessage(DomainMessageEvent event) {
		MessageDto dto = createMessageDto(event);

		if (dto == null) {
			log.warn("MessageEvent 발행 시도 - 이벤트가 null입니다");
			return;
		}
		publishMessageEvent(dto);
	}

	private MessageDto createMessageDto(DomainMessageEvent event) {
		if (event instanceof MeetingMessageEvents.MeetingMessageEvent e) {
			return messageProvider.processMeetingMessage(e);
		} else if (event instanceof PaymentMessageEvents.PaymentEvent e) {
			return messageProvider.processPaymentMessage(e);
		} else if (event instanceof FollowMessageEvents.FollowEvent e) {
			return messageProvider.processFollowMessage(e);
		} else {
			return null;
		}
	}

	//이벤트 발행
	private void publishMessageEvent(MessageDto messageDto) {
		for (Long userId : messageDto.userIdList()) {
			messagePublisher.publish(messageDto.toMessage(userId));
		}
	}
}
