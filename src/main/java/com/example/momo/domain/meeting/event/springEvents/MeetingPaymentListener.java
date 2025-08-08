package com.example.momo.domain.meeting.event.springEvents;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.momo.domain.meeting.application.MeetingPaymentOutboxService;
import com.example.momo.domain.meeting.event.rabbitmq.producer.MeetingProducer;
import com.example.momo.global.springEvent.meeting.MeetingMessageEvents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingPaymentListener {

	private final MeetingPaymentOutboxService service;
	private final MeetingProducer meetingProducer;

	@Retryable(backoff = @Backoff(delay = 1000, multiplier = 2))
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void deleteMeetingEventListener(MeetingMessageEvents.Delete event) {

		try {
			log.info("[Meeting] - MeetingPaymentListener.deleteMeetingEventListener : Meeting Delete 이후 참가자 환불 메세지 발행");

			meetingProducer.deleteMeetingWithRefundsMQ(event);
			service.markEventAsPublished(event.meetingId());
		} catch (Exception e) {
			log.error(
				"[Meeting] : MeetingPaymentListener.deleteMeetingEventListener - Meeting Delete 이후 payment mq 에러가 발생");
			throw e;
		}
	}
}
