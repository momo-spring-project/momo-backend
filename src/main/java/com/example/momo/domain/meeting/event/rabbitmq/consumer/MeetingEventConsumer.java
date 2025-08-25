package com.example.momo.domain.meeting.event.rabbitmq.consumer;

import static com.example.momo.global.rabbitmq.constant.EventTypeNames.*;
import static com.example.momo.global.rabbitmq.constant.QueueNames.*;
import static com.example.momo.global.rabbitmq.constant.RoutingKeys.*;

import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.domain.MeetingParticipant;
import com.example.momo.domain.meeting.domain.MeetingPaymentOutbox;
import com.example.momo.domain.meeting.domain.MeetingPaymentOutboxService;
import com.example.momo.domain.meeting.domain.MeetingService;
import com.example.momo.domain.meeting.event.rabbitmq.producer.MeetingProducer;
import com.example.momo.domain.meeting.exception.MeetingException;
import com.example.momo.global.rabbitmq.dto.common.EventWrapper;
import com.example.momo.global.rabbitmq.dto.meeting.MeetingEvents;
import com.example.momo.global.rabbitmq.dto.payment.PaymentEventMessages;
import com.example.momo.global.webclient.user.UserClient;
import com.example.momo.global.webclient.user.dto.UserClientResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingEventConsumer {

	private final MeetingService meetingService;
	private final UserClient userClient;
	private final MeetingProducer meetingProducer;
	private final ObjectMapper objectMapper;
	private final MeetingPaymentOutboxService meetingPaymentOutboxService;

	// 결제 완료 -> 참가자 추가
	@Transactional
	@RabbitListener(queues = PARTICIPANT_PAYMENT_SUCCEED, containerFactory = "participantListenerContainerFactory")
	public void handlePaymentSuccessEvent(EventWrapper<?> event, Channel channel, Message message) {

		if (event.type() == null || !event.type().equals(PAYMENT_COMPLETED)) {
			log.error("[참가자 이벤트 수신 오류] Required: 결제완료, Received: {}", event);
			throw new RuntimeException("Wrong event type");
		}

		long deliveryTag = message.getMessageProperties().getDeliveryTag();

		try {
			// 멱등 가드 (새 메서드 없이)
			try {
				MeetingPaymentOutbox ob = meetingPaymentOutboxService.getMeetingPaymentOutbox(event.uuId());

				if (Boolean.TRUE.equals(ob.getProcessed())) {
					log.info("[결제완료] 이미 처리된 이벤트, uuid={}", event.uuId());
					channel.basicAck(deliveryTag, false);
					return;
				}
			} catch (IllegalArgumentException noOutbox) {
				log.warn("[결제완료] outbox 없음(uuid={}) - 처리는 계속, 마킹은 스킵 가능", event.uuId());
			}

			log.info("[결제 완료 이벤트 수신] message: {}", event);

			processPaymentSuccessEvent(event);

			// 안전 마킹 (없어도 예외 던지지 않음)
			try {
				meetingPaymentOutboxService.markEventAsProcessed(event.uuId());
			} catch (IllegalArgumentException noOutbox) {
				log.warn("[결제완료] markEventAsProcessed 대상 outbox 없음(uuid={}), 스킵", event.uuId());
			}

			channel.basicAck(deliveryTag, false);
			log.info("[결제 완료 이벤트 처리 완료] event: {}", event);
		} catch (Exception e) {
			log.error("[결제 완료 이벤트 처리 실패] message: {}", event, e);
			try {
				channel.basicNack(deliveryTag, false, false);
			} catch (IOException ex) { /* ignore */ }
			throw new RuntimeException(e);
		}
	}

	// 결제 실패 -> 인원 감소
	@Transactional
	@RabbitListener(queues = PARTICIPANT_PAYMENT_FAILED, containerFactory = "participantListenerContainerFactory")
	public void handlePaymentFailureEvent(EventWrapper<?> event, Channel channel, Message message) {

		if (event.type() == null || !event.type().equals(PAYMENT_FAILED)) {
			log.error("[참가자 이벤트 수신 오류] Required: 결제실패, Received: {}", event);
			throw new RuntimeException("Wrong event type");
		}

		long deliveryTag = message.getMessageProperties().getDeliveryTag();

		try {
			// 멱등 가드
			try {
				MeetingPaymentOutbox ob = meetingPaymentOutboxService.getMeetingPaymentOutbox(event.uuId());
				if (Boolean.TRUE.equals(ob.getProcessed())) {
					log.info("[결제실패] 이미 처리된 이벤트, uuid={}", event.uuId());
					channel.basicAck(deliveryTag, false);
					return;
				}
			} catch (IllegalArgumentException noOutbox) {
				log.warn("[결제실패] outbox 없음(uuid={}) - 처리 계속, 마킹 스킵 가능", event.uuId());
			}

			log.info("[결제 실패 이벤트 수신] event: {}", event);

			processPaymentFailureEvent(event);

			// 안전 마킹
			try {
				meetingPaymentOutboxService.markEventAsProcessed(event.uuId());
			} catch (IllegalArgumentException noOutbox) {
				log.warn("[결제실패] markEventAsProcessed 대상 outbox 없음(uuid={}), 스킵", event.uuId());
			}

			channel.basicAck(deliveryTag, false);
			log.info("[결제 실패 이벤트 처리 완료] message: {}", event);
		} catch (Exception e) {
			log.error("[결제 실패 이벤트 처리 실패] event: {}", event, e);
			try {
				channel.basicNack(deliveryTag, false, false);
			} catch (IOException ex) { /* ignore */ }
			throw new RuntimeException(e);
		}
	}

	@Transactional
	@RabbitListener(queues = PARTICIPANT_DLQ, containerFactory = "participantListenerContainerFactory")
	public void handleParticipantDlq(EventWrapper<?> event, Channel channel, Message message) {
		long tag = message.getMessageProperties().getDeliveryTag();
		try {
			if (event.type() == null) {
				log.error("[참가자 DLQ] type null: {}", event);
				channel.basicAck(tag, false);   // DLQ에서는 소비 후 끝
				return;
			}

			// 멱등 가드
			boolean alreadyProcessed = false;
			try {
				MeetingPaymentOutbox ob = meetingPaymentOutboxService.getMeetingPaymentOutbox(event.uuId());
				alreadyProcessed = Boolean.TRUE.equals(ob.getProcessed());
			} catch (IllegalArgumentException noOutbox) {
				log.warn("[DLQ] outbox 없음(uuid={})", event.uuId());
			}
			if (alreadyProcessed) {
				log.info("[DLQ] 이미 처리된 이벤트 무시: {}", event.uuId());
				channel.basicAck(tag, false);
				return;
			}

			switch (event.type()) {
				case PAYMENT_COMPLETED -> processPaymentSuccessEvent(event);
				case PAYMENT_FAILED -> processPaymentFailureEvent(event);
				default -> log.error("[참가자 DLQ] 해당 이벤트 없음: {}", event.type());
			}

			try {
				meetingPaymentOutboxService.markEventAsProcessed(event.uuId());
			} catch (IllegalArgumentException noOutbox) {
				log.warn("[DLQ] markEventAsProcessed 대상 outbox 없음(uuid={}), 스킵", event.uuId());
			}

			channel.basicAck(tag, false);
		} catch (Exception e) {
			log.error("[참가자 DLQ] 처리 실패. 재큐잉 없이 폐기", e);
			try {
				channel.basicNack(tag, false, false);
			} catch (IOException ignore) {
			}

		}
	}
	// DLQ는 여기서 끝내야 함: 더 던지지 말고 종료

	protected void processPaymentSuccessEvent(EventWrapper<?> event) {

		PaymentEventMessages.Completed message = objectMapper.convertValue(event.data(),
			PaymentEventMessages.Completed.class);

		Long meetingId = message.meetingId();
		Long userId = message.userId();

		try {
			Meeting meeting = meetingService.getMeetingById(meetingId);
			UserClientResponseDto user = userClient.getUser(userId);

			// 참가 완료 이벤트 발행
			meetingProducer.publishParticipantEvents(
				new MeetingEvents.Join(meetingId, userId, meeting.getHostUserId(), user.getNickname()),
				MEETING_PARTICIPANT_JOIN,
				PARTICIPANT_JOIN_KEY
			);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}

	protected void processPaymentFailureEvent(EventWrapper<?> event) {

		PaymentEventMessages.Failed message = objectMapper.convertValue(event.data(),
			PaymentEventMessages.Failed.class);

		Long meetingId = message.meetingId();
		Long userId = message.userId();
		String reason = message.failReason();

		// 이미 결제 완료 등 참가자 감소가 맞지 않는 실패는 무시
		if (reason != null && reason.contains("이미 결제가")) {
			log.info("[결제실패-무시] 이미 결제 완료 사용자: meetingId={}, userId={}", meetingId, userId);
			return;
		}

		try {
			Meeting meeting = meetingService.getMeetingById(meetingId);
			MeetingParticipant participant = meetingService.getParticipantByMeetingIdAndUserId(meetingId,
				message.userId());
			meeting.removeMeetingParticipant(participant);
		} catch (MeetingException notFound) {
			log.info("[결제실패-무시] 참가자 없음: meetingId={}, userId={}", meetingId, userId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}
}