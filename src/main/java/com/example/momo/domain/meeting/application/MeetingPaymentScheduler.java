package com.example.momo.domain.meeting.application;

import static com.example.momo.global.rabbitmq.constant.EventTypeNames.*;

import java.util.List;

import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.domain.MeetingParticipant;
import com.example.momo.global.rabbitmq.dto.meeting.ParticipantEvents;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.momo.domain.meeting.domain.MeetingPaymentOutbox;
import com.example.momo.domain.meeting.event.rabbitmq.producer.MeetingProducer;
import com.example.momo.global.rabbitmq.dto.common.EventWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingPaymentScheduler {

	private final MeetingPaymentOutboxService meetingPaymentOutboxService;
	private final MeetingProducer meetingProducer;
	private final ObjectMapper objectMapper;
	private final MeetingService meetingService;

	// 미발행 이벤트 재시도, 1분 주기
	@Scheduled(fixedRate = 60_000)
	public void retryUnpublishedEvents() {

		List<MeetingPaymentOutbox> list = meetingPaymentOutboxService.getUnpublishedPaymentOutbox();

		for (MeetingPaymentOutbox outbox : list) {

			if (outbox.getEventType().equals(MEETING_DELETE)) {
				try {
					EventWrapper<?> wrapper =
						objectMapper.readValue(outbox.getPayload(),
							new TypeReference<>() {
							});

					EventWrapper<?> retryWrapper = EventWrapper.of(wrapper.uuId(), MEETING_DELETE,
						wrapper.data());

					meetingProducer.deleteMeetingWithRefundsMQ(retryWrapper);
					meetingPaymentOutboxService.markEventAsPublished(retryWrapper.uuId());
				} catch (Exception e) {
					log.error(
						"[Meeting] : MeetingPaymentScheduler.retryUnpublishedEvents - Meeting 취소시 환불 메세지 아웃박스 스케줄러 실패 2");
					throw new RuntimeException(e);
				}
			}
		}
	}

	// 유실된 이벤트 롤백, 10분 주기
	@Transactional
	@Scheduled(fixedRate = 600_000)
	public void rollbackLostMeetingEvents() {
		List<MeetingPaymentOutbox> list = meetingPaymentOutboxService.getUnProcessedPaymentOutbox();

		for (MeetingPaymentOutbox outbox : list) {
			try {
				String eventType = outbox.getEventType();
				switch (eventType) {
					case MEETING_PARTICIPANT_REGISTER -> {
						ParticipantEvents.Register event = objectMapper.readValue(
							outbox.getPayload(),
							ParticipantEvents.Register.class
						);
						Meeting meeting = meetingService.getMeetingById(event.meetingId());
						MeetingParticipant participant = meetingService.getParticipantByMeetingIdAndUserId(event.meetingId(), event.userId());
						meeting.removeMeetingParticipant(participant);
						log.info("[Meeting] : 유실된 ParticipantEvents.Register 롤백, outbox = {}", outbox);
					}
					case MEETING_PARTICIPANT_CANCEL -> {
						ParticipantEvents.Cancel event = objectMapper.readValue(
							outbox.getPayload(),
							ParticipantEvents.Cancel.class
						);
						Meeting meeting = meetingService.getMeetingById(event.meetingId());
						MeetingParticipant participant = meetingService.getParticipantByMeetingIdAndUserId(event.meetingId(), event.userId());
						meeting.addMeetingParticipant(participant);
						log.info("[Meeting] : 유실된 ParticipantEvents.Cancel 롤백, outbox = {}", outbox);
					}
				}
			} catch (Exception e) {
				log.error("[Meeting] : MeetingPaymentScheduler.rollbackLostMeetingEvents - 유실된 메세지 처리 실패");
				throw new RuntimeException(e);
			}
		}
	}
}
