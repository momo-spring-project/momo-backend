package com.example.momo.domain.meeting.event.springEvents;

import static com.example.momo.global.rabbitmq.constant.EventTypeNames.*;
import static com.example.momo.global.rabbitmq.constant.RoutingKeys.*;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.momo.domain.meeting.domain.MeetingPaymentOutboxService;
import com.example.momo.domain.meeting.event.rabbitmq.producer.MeetingProducer;
import com.example.momo.global.rabbitmq.dto.common.EventWrapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingPaymentListener {

	private final MeetingPaymentOutboxService service;
	private final MeetingProducer meetingProducer;

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

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void registerParticipantEventListener(EventWrapper<?> wrapper) {

		if (!wrapper.type().equals(MEETING_PARTICIPANT_REGISTER)) {
			return;
		}

		try {
			log.info("[Meeting] - MeetingPaymentListener.registerParticipantEventListener : 참가자 신청 메세지 발행");

			boolean ack = meetingProducer.publishWithConfirmParticipantEvents(wrapper, PARTICIPANT_REGISTER_KEY);
			if (ack) {
				service.markEventAsPublished(wrapper.uuId());   // ACK일 때만
			} else {
				throw new RuntimeException("publish confirm not ack"); // @Retryable 재시도
			}
		} catch (Exception e) {
			log.error(
				"[Meeting] : MeetingPaymentListener.registerParticipantEventListener - 참가자 신청 MQ 에러가 발생");
			throw e;
		}
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void cancelParticipantEventListener(EventWrapper<?> wrapper) {

		if (!wrapper.type().equals(MEETING_PARTICIPANT_CANCEL)) {
			return;
		}

		try {
			log.info("[Meeting] - MeetingPaymentListener.cancelParticipantEventListener : 참가자 신청 메세지 발행");

			boolean ack = meetingProducer.publishWithConfirmParticipantEvents(wrapper, PARTICIPANT_CANCEL_KEY);
			if (ack) {
				service.markEventAsPublished(wrapper.uuId());   // ACK일 때만
			} else {
				throw new RuntimeException("publish confirm not ack"); // @Retryable 재시도
			}
		} catch (Exception e) {
			log.error(
				"[Meeting] : MeetingPaymentListener.cancelParticipantEventListener - 참가자 신청 MQ 에러가 발생");
			throw e;
		}
	}
}