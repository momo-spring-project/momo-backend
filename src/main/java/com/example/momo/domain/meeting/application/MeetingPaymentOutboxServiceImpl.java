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
	public void savePaymentOutbox(MeetingPaymentOutbox outbox) {

		repository.save(outbox);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markEventAsPublished(Long meetingId) {

		MeetingPaymentOutbox event = repository.findById(meetingId)
			.orElseThrow(() -> new IllegalArgumentException("no outbox: " + meetingId));
		event.markAsPublished();
	}

	@Override
	public List<MeetingPaymentOutbox> getUnpublishedPaymentOutbox() {
		return repository.findUnpublishedEvents();
	}
}
