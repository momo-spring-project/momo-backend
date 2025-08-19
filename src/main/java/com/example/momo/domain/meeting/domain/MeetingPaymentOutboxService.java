package com.example.momo.domain.meeting.domain;

import java.util.List;

public interface MeetingPaymentOutboxService {

	void savePaymentOutbox(MeetingPaymentOutbox outbox);

	void markEventAsPublished(String eventUuid);

	List<MeetingPaymentOutbox> getUnpublishedPaymentOutbox();

	void markEventAsProcessed(String uuId);

	List<MeetingPaymentOutbox> getUnProcessedPaymentOutbox();

	MeetingPaymentOutbox getMeetingPaymentOutbox(String uuid);
}
