package com.example.momo.domain.meeting.event.rabbitmq.consumer;

import com.example.momo.domain.meeting.application.MeetingReader;
import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.domain.MeetingParticipant;
import com.example.momo.domain.meeting.domain.MeetingRepository;
import com.example.momo.domain.meeting.event.rabbitmq.producer.MeetingEventPublisher;
import com.example.momo.domain.payment.event.rabbitmq.dto.PaymentEventDto;
import com.example.momo.global.rabbitmq.constant.QueueNames;
import com.example.momo.global.rabbitmq.dto.ParticipantEvents;
import com.example.momo.global.webclient.user.UserClient;
import com.example.momo.global.webclient.user.dto.UserClientResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingEventConsumer {

	private final MeetingReader meetingReader;
	private final UserClient userClient;
	private final MeetingEventPublisher meetingEventPublisher;
	private final MeetingRepository meetingRepository;
	private final ObjectMapper objectMapper;

	// 결제 완료 -> 참가자 추가
	@RabbitListener(queues = QueueNames.PARTICIPANT_PAYMENT_SUCCESS, containerFactory = "participantListenerContainerFactory")
	public void handlePaymentSuccessEvent(PaymentEventDto event) {
		processPaymentSuccessEvent(event);
	}

	// 결제 실패 -> 인원 감소
	@RabbitListener(queues = QueueNames.PARTICIPANT_PAYMENT_FAIL, containerFactory = "participantListenerContainerFactory")
	public void handlePaymentFailureEvent(PaymentEventDto event) {
		processPaymentFailureEvent(event);
	}

	/**
	 * 현재 DLQ 들어오는 목록
	 * 큐: PARTICIPANT_PAYMENT_SUCCESS, PARTICIPANT_PAYMENT_FAIL
	 * DTO: PaymentEventDto
	 * 분기판단: String eventType
	 */
	@RabbitListener(queues = QueueNames.X_DLQ_PARTICIPANT)
	public void handleParticipantDlq(String message) {
		try {
			JsonNode json = objectMapper.readTree(message);
			String type = json.get("eventType").asText();

			switch (type) {
				case "PAYMENT_COMPLETED" -> {
					PaymentEventDto success = objectMapper.treeToValue(json, PaymentEventDto.class);
						processPaymentSuccessEvent(success);
				}
				case "PAYMENT_FAILED" -> {
					PaymentEventDto fail = objectMapper.treeToValue(json, PaymentEventDto.class);
					processPaymentFailureEvent(fail);
				}
				default -> log.error("Unsupported event type: {}", type);
			}
		} catch (Exception e) {
			log.error("Dlq 처리 실패 : {}", message);
		}
	}

	private void processPaymentSuccessEvent(PaymentEventDto event) {
		try {
			Long meetingId = event.getMeetingId();
			Long userId = event.getUserId();

			Meeting meeting = meetingReader.getMeetingById(meetingId);
			UserClientResponseDto user = userClient.getUser(userId);

			// 참가자 추가
			MeetingParticipant participant = MeetingParticipant.createParticipant(meeting.getId(), userId);
			meetingRepository.saveParticipant(participant);

			// 참가 완료 이벤트 발행
			meetingEventPublisher.publishParticipantEvents(
				new ParticipantEvents.Join(meetingId, userId, meeting.getHostUserId(), user.getNickname())
			);
		} catch (Exception e) {
			// dlq 보내서 재시도
			log.error(e.getMessage(), e);
			throw e;
		}
	}

	private void processPaymentFailureEvent(PaymentEventDto event) {
		try {
			Meeting meeting = meetingReader.getMeetingById(event.getMeetingId());
			meeting.removeMeetingParticipant();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}
}
