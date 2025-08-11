package com.example.momo.domain.meeting.event.rabbitmq.consumer;

import com.example.momo.domain.meeting.application.MeetingReader;
import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.domain.MeetingParticipant;
import com.example.momo.domain.meeting.event.rabbitmq.producer.MeetingEventPublisher;
import com.example.momo.global.rabbitmq.dto.meeting.ParticipantEvents;
import com.example.momo.global.rabbitmq.dto.common.EventWrapper;
import com.example.momo.global.rabbitmq.dto.payment.PaymentEventMessages;
import com.example.momo.global.webclient.user.UserClient;
import com.example.momo.global.webclient.user.dto.UserClientResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.rabbitmq.client.Channel;

import static com.example.momo.global.rabbitmq.constant.EventTypeNames.*;
import static com.example.momo.global.rabbitmq.constant.QueueNames.*;
import static com.example.momo.global.rabbitmq.constant.RoutingKeys.PARTICIPANT_JOIN_KEY;

// Dto 수정해야함

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingEventConsumer {

	private final MeetingReader meetingReader;
	private final UserClient userClient;
	private final MeetingEventPublisher meetingEventPublisher;
	private final ObjectMapper objectMapper;

	/**
	 * 이벤트 수신 과정
	 * 1. EventWrapper<?> event 수신
	 * 2. type(EventTypeNames) 확인
	 * 3. JsonNode 로 변환
	 * 4. json.data 의 필드값으로 조회해서 값 사용
	 */

	// 결제 완료 -> 참가자 추가
	@Transactional
	@RabbitListener(queues = PARTICIPANT_PAYMENT_SUCCEED, containerFactory = "participantListenerContainerFactory")
	public void handlePaymentSuccessEvent(EventWrapper<?> event, Channel channel, Message message) {

		if (event.type() == null || !event.type().equals(PAYMENT_COMPLETED)) {
			log.error("[참가자 이벤트 수신 오류] Required: 결제완료, Received: {}", event);
			throw new RuntimeException("Wrong event type");
		}

		long deliveryTag = message.getMessageProperties().getDeliveryTag();
		log.info("[결제 완료 이벤트 수신] message: {}", event);

		try {
			processPaymentSuccessEvent(event);
			channel.basicAck(deliveryTag, false);
			log.info("[결제 완료 이벤트 처리 완료] event: {}", event);
		} catch (Exception e) {
			log.error("[결제 완료 이벤트 처리 실패] message: {}", event, e);
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
		log.info("[결제 실패 이벤트 수신] event: {}", event);

		try {
			processPaymentFailureEvent(event);
			channel.basicAck(deliveryTag, false);
			log.info("[결제 실패 이벤트 처리 완료] message: {}", event);
		} catch (Exception e) {
			log.error("[결제 실패 이벤트 처리 실패] event: {}", event, e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 현재 DLQ 들어오는 목록
	 * 큐: PARTICIPANT_PAYMENT_SUCCESS, PARTICIPANT_PAYMENT_FAIL
	 * 작동하는지 확인 불가
	 */
	@Transactional
	@RabbitListener(queues = DLQ_PARTICIPANT)
	public void handleParticipantDlq(EventWrapper<?> event) {

		if (event.type() == null) {
			log.error("[참가자 DLQ] Received: {}", event);
			throw new RuntimeException("Wrong event type");
		}
		String type = event.type();

		try {
			switch (type) {
				case PAYMENT_COMPLETED -> processPaymentSuccessEvent(event);
				case PAYMENT_FAILED -> processPaymentFailureEvent(event);
				default -> log.error("[참가자 DLQ] 해당하는 이벤트가 없습니다");
			}
		} catch (Exception e) {
			log.error("[Dlq 처리 실패] : event: {}", event);
			throw e;
		}
	}

	protected void processPaymentSuccessEvent(EventWrapper<?> event) {

		PaymentEventMessages.Completed message = objectMapper.convertValue(event.data(), PaymentEventMessages.Completed.class);

		Long meetingId = message.meetingId();
		Long userId = message.userId();

		try {
			Meeting meeting = meetingReader.getMeetingById(meetingId);
			UserClientResponseDto user = userClient.getUser(userId);

			// 참가자 추가
			MeetingParticipant participant = MeetingParticipant.createParticipant(meeting, userId);
			meeting.getParticipants().add(participant);

			// 참가 완료 이벤트 발행
			meetingEventPublisher.publishParticipantEvents(
				new ParticipantEvents.Join(meetingId, userId, meeting.getHostUserId(), user.getNickname()),
				MEETING_PARTICIPANT_JOIN,
				PARTICIPANT_JOIN_KEY
			);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}

	protected void processPaymentFailureEvent(EventWrapper<?> event) {

		PaymentEventMessages.Failed message = objectMapper.convertValue(event.data(), PaymentEventMessages.Failed.class);

		Long meetingId = message.meetingId();

		try {
			Meeting meeting = meetingReader.getMeetingById(meetingId);
			meeting.removeMeetingParticipant();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}
}
