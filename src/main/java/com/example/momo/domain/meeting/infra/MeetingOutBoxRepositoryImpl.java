package com.example.momo.domain.meeting.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.momo.domain.meeting.domain.MeetingElasticsearchOutbox;
import com.example.momo.domain.meeting.domain.MeetingOutboxRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MeetingOutBoxRepositoryImpl implements MeetingOutboxRepository {

	private final MeetingOutboxJpaRepository meetingOutboxJpaRepository;

	@Override
	public void save(MeetingElasticsearchOutbox outbox) {
		meetingOutboxJpaRepository.save(outbox);
	}

	@Override
	public List<MeetingElasticsearchOutbox> findUnpublishedEvents() {
		return meetingOutboxJpaRepository.findByPublishedFalseOrderByCreatedAt();
	}

	@Override
	public Optional<MeetingElasticsearchOutbox> findById(Long id) {
		return meetingOutboxJpaRepository.findById(id);
	}
}
