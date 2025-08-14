package com.example.momo.domain.meeting.event.springEvents;

import static com.example.momo.global.rabbitmq.constant.EventTypeNames.*;

import com.example.momo.global.rabbitmq.dto.meeting.MeetingEvents;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.momo.domain.meeting.application.MeetingPaymentOutboxService;
import com.example.momo.domain.meeting.event.rabbitmq.producer.MeetingProducer;
import com.example.momo.global.rabbitmq.dto.common.EventWrapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.example.momo.global.rabbitmq.constant.EventTypeNames.MEETING_PARTICIPANT_CANCEL;
import static com.example.momo.global.rabbitmq.constant.EventTypeNames.MEETING_PARTICIPANT_REGISTER;
import static com.example.momo.global.rabbitmq.constant.RoutingKeys.PARTICIPANT_CANCEL_KEY;
import static com.example.momo.global.rabbitmq.constant.RoutingKeys.PARTICIPANT_REGISTER_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingPaymentListener {

	private final MeetingPaymentOutboxService service;
	private final MeetingProducer meetingProducer;

	@Retryable(backoff = @Backoff(delay = 1000, multiplier = 2))
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void deleteMeetingEventListener(EventWrapper<?> wrapper) {

		if (!wrapper.type().equals(MEETING_DELETE))
			return;

		try {
			log.info("[Meeting] - MeetingPaymentListener.deleteMeetingEventListener : Meeting Delete 이후 참가자 환불 메세지 발행");

			meetingProducer.deleteMeetingWithRefundsMQ(wrapper);
			service.markEventAsPublished(wrapper.uuId());
		} catch (Exception e) {
			log.error(
				"[Meeting] : MeetingPaymentListener.deleteMeetingEventListener - Meeting Delete 이후 payment mq 에러가 발생");
			throw e;
		}
	}

	@Retryable(backoff = @Backoff(delay = 1000, multiplier = 2))
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void registerParticipantEventListener(MeetingEvents.Register event) {

		try {
			log.info("[Meeting] - MeetingPaymentListener.registerParticipantEventListener : 참가자 신청 메세지 발행");

			meetingProducer.publishWithConfirmParticipantEvents(
				event,
				MEETING_PARTICIPANT_REGISTER,
				PARTICIPANT_REGISTER_KEY
			);
		} catch (Exception e) {
			log.error(
				"[Meeting] : MeetingPaymentListener.registerParticipantEventListener - 참가자 신청 MQ 에러가 발생");
			throw e;
		}
	}

	@Retryable(backoff = @Backoff(delay = 1000, multiplier = 2))
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void cancelParticipantEventListener(MeetingEvents.Cancel event) {

		try {
			log.info("[Meeting] - MeetingPaymentListener.cancelParticipantEventListener : 참가자 신청 메세지 발행");

			meetingProducer.publishWithConfirmParticipantEvents(
				event,
				MEETING_PARTICIPANT_CANCEL,
				PARTICIPANT_CANCEL_KEY
			);
		} catch (Exception e) {
			log.error(
				"[Meeting] : MeetingPaymentListener.cancelParticipantEventListener - 참가자 신청 MQ 에러가 발생");
			throw e;
		}
	}
}
