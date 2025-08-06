package com.example.momo.domain.meeting.event.rabbitmq.consumer;

import com.example.momo.domain.meeting.application.MeetingReader;
import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.domain.MeetingParticipant;
import com.example.momo.domain.meeting.domain.MeetingRepository;
import com.example.momo.domain.meeting.event.rabbitmq.producer.MeetingEventPublisher;
import com.example.momo.domain.payment.event.rabbitmq.dto.PaymentEventDto;
import com.example.momo.global.rabbitmq.dto.ParticipantEvents;
import com.example.momo.global.webclient.user.UserClient;
import com.example.momo.global.webclient.user.dto.UserClientResponseDto;
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

	// 임시 Dto, 수정 예정
	// 결제 완료 -> 참가자 추가
	@RabbitListener(queues = "payment.participant.created.queue", ackMode = "MANUAL")
	public void handlePaymentSuccessEvent(PaymentEventDto event) {
		try {
			Long meetingId = event.getMeetingId();
			Long userId = event.getUserId();

			Meeting meeting = meetingReader.getMeetingById(meetingId);
			UserClientResponseDto user = userClient.getUser(userId);

			// 참가자 추가
			MeetingParticipant participant = new MeetingParticipant(meeting.getId(), userId);
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

	// 추가 예정
	// 결제 취소 -> 인원 감소

	// 결제 실패 -> 인원 유지
}
