package com.example.momo.domain.meeting.domain;

import java.util.List;
import java.util.Optional;

public interface MeetingPaymentOutboxRepository {

	void save(MeetingPaymentOutbox outbox);

	List<MeetingPaymentOutbox> findUnpublishedEvents();

	Optional<MeetingPaymentOutbox> findById(Long id);
}
