package com.example.momo.domain.meeting.application;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.momo.domain.meeting.domain.MeetingPaymentOutbox;
import com.example.momo.domain.meeting.event.rabbitmq.producer.MeetingProducer;
import com.example.momo.global.springEvent.meeting.MeetingMessageEvents;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingPaymentScheduler {

	private final MeetingPaymentOutboxService meetingPaymentOutboxService;
	private final MeetingProducer meetingProducer;

	@Scheduled(fixedRate = 60_000)
	public void retryUnpublishedEvents() {

		List<MeetingPaymentOutbox> list = meetingPaymentOutboxService.getUnpublishedPaymentOutbox();
		ObjectMapper objectMapper = new ObjectMapper();

		for (MeetingPaymentOutbox outbox : list) {

			try {
				MeetingMessageEvents.Delete event = objectMapper.readValue(outbox.getPayload(),
					MeetingMessageEvents.Delete.class);
				meetingProducer.deleteMeetingWithRefundsMQ(event);
				meetingPaymentOutboxService.markEventAsPublished(outbox.getMeetingId());
			} catch (Exception e) {
				log.error(
					"[Meeting] : MeetingPaymentScheduler.retryUnpublishedEvents - Meeting 취소시 환불 메세지 아웃박스 스케줄러 실패");
				throw new RuntimeException(e);
			}
		}
	}
}
