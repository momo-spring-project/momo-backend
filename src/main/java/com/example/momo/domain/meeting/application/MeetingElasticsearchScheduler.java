package com.example.momo.domain.meeting.application;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.domain.MeetingElasticsearchOutbox;
import com.example.momo.domain.meeting.enums.ElasticsearchEventType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingElasticsearchScheduler {

	private final MeetingOutboxService meetingOutboxService;
	private final MeetingService meetingService;

	@Scheduled(fixedRate = 60_000)
	public void retryUnpublishedEvents() {

		List<MeetingElasticsearchOutbox> unpublishedEvents =
			meetingOutboxService.getUnpublishedMeetingOutbox();

		for (MeetingElasticsearchOutbox outbox : unpublishedEvents) {
			try {
				Meeting meeting = meetingService.getMeetingEntity(outbox.getMeetingId());

				if (outbox.getEventType().equals(ElasticsearchEventType.SAVE)) {
					meetingService.createElasticMeeting(meeting);
				}

				if (outbox.getEventType().equals(ElasticsearchEventType.DELETE)) {
					meetingService.deleteElasticMeeting(meeting);
				}

				meetingOutboxService.markEventAsPublished(outbox.getId());
			} catch (Exception e) {

				log.error(
					"[Meeting] : MeetingElasticsearchScheduler.retryUnpublishedEvents - Elasticsearch 아웃박스 재시도 스케줄러 내부 오류");
				throw new RuntimeException(e);
			}
		}
	}
}
