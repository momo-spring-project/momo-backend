package com.example.momo.domain.meeting.application;

import java.util.List;

import com.example.momo.domain.meeting.domain.MeetingElasticsearchOutbox;

public interface MeetingOutboxService {

	void saveMeetingOutbox(MeetingElasticsearchOutbox outbox);

	void markEventAsPublished(Long outboxId);

	List<MeetingElasticsearchOutbox> getUnpublishedMeetingOutbox();
}
