package com.example.momo.domain.meeting.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.meeting.domain.MeetingElasticsearchOutbox;
import com.example.momo.domain.meeting.domain.MeetingOutboxRepository;
import com.example.momo.domain.meeting.domain.MeetingOutboxService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingOutboxServiceImpl implements MeetingOutboxService {

	private final MeetingOutboxRepository meetingOutboxRepository;

	@Override
	public void saveMeetingOutbox(MeetingElasticsearchOutbox outbox) {

		meetingOutboxRepository.save(outbox);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markEventAsPublished(Long outboxId) {

		MeetingElasticsearchOutbox event = meetingOutboxRepository.findById(outboxId)
			.orElseThrow(() -> new IllegalArgumentException("no outbox: " + outboxId));
		event.markAsPublished();
	}

	@Override
	public List<MeetingElasticsearchOutbox> getUnpublishedMeetingOutbox() {

		return meetingOutboxRepository.findUnpublishedEvents();
	}
}
