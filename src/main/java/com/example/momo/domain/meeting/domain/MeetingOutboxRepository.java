package com.example.momo.domain.meeting.domain;

import java.util.List;
import java.util.Optional;

public interface MeetingOutboxRepository {

	void save(MeetingElasticsearchOutbox outbox);

	List<MeetingElasticsearchOutbox> findUnpublishedEvents();

	Optional<MeetingElasticsearchOutbox> findById(Long id);
}
