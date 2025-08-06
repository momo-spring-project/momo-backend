package com.example.momo.domain.messagehub.application.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.momo.global.rabbitmq.dto.follow.FollowAlarmMessages;
import com.example.momo.global.rabbitmq.dto.meeting.MeetingAlarmMessages;
import com.example.momo.global.rabbitmq.dto.payment.PaymentAlarmMessages;

import lombok.extern.slf4j.Slf4j;

/**
 * todo : 삭제예정
 */
@Component
@Slf4j
public class DomainEventListener {

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleMeetingMessageEvent(MeetingAlarmMessages.MeetingAlarmMessage event) {
		log.warn("현재 사용되지 않는 이벤트 - RabbitListener 로 변경 필요");
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleFollowMessageEvent(FollowAlarmMessages.FollowAlarmMessage event) {
		log.warn("현재 사용되지 않는 이벤트 - RabbitListener 로 변경 필요");

	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePaymentMessageEvent(PaymentAlarmMessages.PaymentAlarmMessage event) {
		log.warn("현재 사용되지 않는 이벤트 - RabbitListener 로 변경 필요");
	}
}
