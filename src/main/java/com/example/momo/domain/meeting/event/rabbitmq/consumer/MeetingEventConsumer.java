package com.example.momo.domain.meeting.event.rabbitmq.consumer;

import com.example.momo.domain.meeting.application.MeetingReader;
import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.domain.MeetingParticipant;
import com.example.momo.domain.meeting.domain.MeetingRepository;
import com.example.momo.domain.meeting.event.rabbitmq.producer.MeetingEventPublisher;
import com.example.momo.global.rabbitmq.constant.QueueNames;
import com.example.momo.global.rabbitmq.dto.ParticipantEvents;
import com.example.momo.global.rabbitmq.dto.PaymentEventMessage;
import com.example.momo.global.webclient.user.UserClient;
import com.example.momo.global.webclient.user.dto.UserClientResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.rabbitmq.client.Channel;

import static com.example.momo.global.rabbitmq.constant.QueueNames.*;

// Dto 수정해야함

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingEventConsumer {

	private final MeetingReader meetingReader;
	private final UserClient userClient;
	private final MeetingEventPublisher meetingEventPublisher;
	private final MeetingRepository meetingRepository;
	private final ObjectMapper objectMapper;

	// 전체적으로 예외처리 어떻게할지

	// 결제 완료 -> 참가자 추가
	@Transactional
	@RabbitListener(queues = PARTICIPANT_PAYMENT_SUCCESS, containerFactory = "participantListenerContainerFactory")
	public void handlePaymentSuccessEvent(PaymentEventMessage.Completed event, Channel channel, Message message) {
		long deliveryTag = message.getMessageProperties().getDeliveryTag();
		log.info("[결제 완료 이벤트 수신] message: {}", event);
		try {
			processPaymentSuccessEvent(event);
			channel.basicAck(deliveryTag, false);
			log.info("[결제 완료 이벤트 처리 완료] message: {}", event);
		} catch (Exception e) {
			log.error("[결제 완료 이벤트 처리 실패] message: {}", event, e);
			throw new RuntimeException(e);
		}
	}

	// 결제 실패 -> 인원 감소
	@Transactional
	@RabbitListener(queues = PARTICIPANT_PAYMENT_FAIL, containerFactory = "participantListenerContainerFactory")
	public void handlePaymentFailureEvent(PaymentEventMessage.Failed event, Channel channel, Message message) {
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
	public void handleParticipantDlq(PaymentEventMessage message) {
		try {
			if (message instanceof PaymentEventMessage.Completed completed) {
				processPaymentSuccessEvent(completed);
			} else if (message instanceof PaymentEventMessage.Failed failed) {
				processPaymentFailureEvent(failed);
			}
		} catch (Exception e) {
			log.error("[Dlq 처리 실패] : message: {}", message);
			throw e;
		}
	}

	protected void processPaymentSuccessEvent(PaymentEventMessage.Completed event) {
		try {
			Long meetingId = event.getMeetingId();
			Long userId = event.getUserId();

			Meeting meeting = meetingReader.getMeetingById(meetingId);
			UserClientResponseDto user = userClient.getUser(userId);

			// 참가자 추가
			MeetingParticipant participant = MeetingParticipant.createParticipant(meeting.getId(), userId);
			meeting.getParticipants().add(participant);

			// 참가 완료 이벤트 발행
			meetingEventPublisher.publishParticipantEvents(
				new ParticipantEvents.Join(meetingId, userId, meeting.getHostUserId(), user.getNickname())
			);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}

	protected void processPaymentFailureEvent(PaymentEventMessage.Failed event) {
		try {
			Meeting meeting = meetingReader.getMeetingById(event.getMeetingId());
			meeting.removeMeetingParticipant();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}
}
