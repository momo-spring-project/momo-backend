package com.example.momo.domain.meeting.infra.meeting;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.momo.domain.meeting.domain.MeetingPaymentOutbox;
import com.example.momo.domain.meeting.domain.MeetingPaymentOutboxRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MeetingPaymentOutboxRepositoryImpl implements MeetingPaymentOutboxRepository {

	private final MeetingPaymentOutboxJpaRepository jpaRepository;

	@Override
	public void save(MeetingPaymentOutbox outbox) {
		jpaRepository.save(outbox);
	}

	@Override
	public List<MeetingPaymentOutbox> findUnpublishedEvents() {
		return jpaRepository.findByPublishedFalseOrderByCreatedAt();
	}

	@Override
	public Optional<MeetingPaymentOutbox> findById(Long id) {
		return jpaRepository.findByMeetingId(id);
	}

	@Override
	public Optional<MeetingPaymentOutbox> findByEventUuid(String eventUuid) {
		return jpaRepository.findByEventUuid(eventUuid);
	}

	@Override
	public List<MeetingPaymentOutbox> findUnProcessedEvents() {
		return jpaRepository.findByPublishedTrueAndProcessedFalse();
	}
}
