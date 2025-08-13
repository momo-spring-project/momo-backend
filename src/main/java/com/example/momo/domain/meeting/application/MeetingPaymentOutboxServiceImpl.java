package com.example.momo.domain.meeting.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.meeting.domain.MeetingPaymentOutbox;
import com.example.momo.domain.meeting.domain.MeetingPaymentOutboxRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingPaymentOutboxServiceImpl implements MeetingPaymentOutboxService {

	private final MeetingPaymentOutboxRepository repository;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void savePaymentOutbox(MeetingPaymentOutbox outbox) {
		repository.save(outbox);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markEventAsPublished(String eventUuid) {
		MeetingPaymentOutbox event = repository.findByEventUuid(eventUuid)
			.orElseThrow(() -> new IllegalArgumentException("no outbox: " + eventUuid));
		event.markAsPublished();
	}

	@Override
	public List<MeetingPaymentOutbox> getUnpublishedPaymentOutbox() {
		return repository.findUnpublishedEvents();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markEventAsProcessed(String uuId) {
		MeetingPaymentOutbox event = repository.findByEventUuid(uuId)
			.orElseThrow(() -> new IllegalArgumentException("no outbox: " + uuId));
		event.markAsProcessed();
	}

	@Override
	public List<MeetingPaymentOutbox> getUnProcessedPaymentOutbox() {
		return repository.findUnProcessedEvents();
	}
}
