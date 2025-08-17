package com.example.momo.domain.meeting.domain;

import java.util.List;

public interface MeetingOutboxService {

	void saveMeetingOutbox(MeetingElasticsearchOutbox outbox);

	void markEventAsPublished(Long outboxId);

	List<MeetingElasticsearchOutbox> getUnpublishedMeetingOutbox();
}
