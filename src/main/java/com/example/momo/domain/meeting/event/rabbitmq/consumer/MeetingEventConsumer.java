package com.example.momo.domain.meeting.event.rabbitmq.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.meeting.application.MeetingReader;
import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.domain.MeetingParticipant;
import com.example.momo.domain.meeting.domain.MeetingRepository;
import com.example.momo.domain.meeting.event.rabbitmq.producer.MeetingEventPublisher;
import com.example.momo.domain.payment.event.springEvent.PaymentEventDto;
import com.example.momo.global.rabbitmq.dto.ParticipantEvents;
import com.example.momo.global.webclient.user.UserClient;
import com.example.momo.global.webclient.user.dto.UserClientResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingEventConsumer {

	private final MeetingReader meetingReader;
	private final UserClient userClient;
	private final MeetingEventPublisher meetingEventPublisher;
	private final MeetingRepository meetingRepository;

	/**
	 * 결제 완료 이벤트 처리
	 * Queue: meeting.payment.completed.queue
	 *
	 * 결제가 완료되면 참가자를 실제로 등록하고 알림 이벤트 발행
	 */
	@RabbitListener(queues = "meeting.payment.completed.queue")
	@Transactional
	public void handlePaymentCompleted(PaymentEventDto event) {
		log.info("[결제 완료 수신] meetingId: {}, userId: {}, amount: {}원",
			event.getMeetingId(), event.getUserId(), event.getAmount());

		try {
			Long meetingId = event.getMeetingId();
			Long userId = event.getUserId();

			// 이미 참가자로 등록되어 있는지 확인 (중복 방지)
			if (meetingRepository.existsByMeetingIdAndUserId(meetingId, userId)) {
				log.warn("이미 참가자로 등록됨 - meetingId: {}, userId: {}", meetingId, userId);
				return;
			}

			Meeting meeting = meetingReader.getMeetingById(meetingId);
			UserClientResponseDto user = userClient.getUser(userId);

			// 참가자 추가
			MeetingParticipant participant = new MeetingParticipant(meetingId, userId);
			meetingRepository.saveParticipant(participant);

			log.info("[참가자 등록 완료] meetingId: {}, userId: {}, participantId: {}",
				meetingId, userId, participant.getId());

			// 참가 완료 알림 이벤트 발행 (Notification 서비스용)
			meetingEventPublisher.publishParticipantEvents(
				new ParticipantEvents.Join(
					meetingId,
					userId,
					meeting.getHostUserId(),
					user.getNickname()
				)
			);

			log.info("[참가 완료 이벤트 발행] meetingId: {}, userId: {}", meetingId, userId);

		} catch (Exception e) {
			log.error("[결제 완료 처리 실패] meetingId: {}, userId: {}, error: {}",
				event.getMeetingId(), event.getUserId(), e.getMessage(), e);
			throw e; // 트랜잭션 롤백 및 메시지 재처리
		}
	}

	/**
	 * 결제 실패 이벤트 처리
	 * Queue: meeting.payment.failed.queue
	 *
	 * 결제가 실패하면 예약했던 자리를 복구
	 */
	@RabbitListener(queues = "meeting.payment.failed.queue")
	@Transactional
	public void handlePaymentFailed(PaymentEventDto event) {
		log.info("[결제 실패 수신] meetingId: {}, userId: {}, failReason: {}",
			event.getMeetingId(), event.getUserId(), event.getFailReason());

		try {
			Long meetingId = event.getMeetingId();

			Meeting meeting = meetingReader.getMeetingById(meetingId);

			// 인원수 복구 (결제 시작할 때 증가시켰던 인원 감소)
			meeting.removeMeetingParticipant();

			log.info("[인원수 복구 완료] meetingId: {}, 현재 인원: {}/{}",
				meetingId,
				meeting.getCurrentParticipantsCount(),
				meeting.getMaxParticipantsCount());
			
		} catch (Exception e) {
			log.error("[결제 실패 처리 중 오류] meetingId: {}, userId: {}, error: {}",
				event.getMeetingId(), event.getUserId(), e.getMessage(), e);
			throw e; // 트랜잭션 롤백 및 메시지 재처리
		}
	}

}

