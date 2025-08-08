package com.example.momo.domain.meeting.application;

import java.util.List;

import com.example.momo.domain.meeting.domain.MeetingPaymentOutbox;

public interface MeetingPaymentOutboxService {

	void savePaymentOutbox(MeetingPaymentOutbox outbox);

	void markEventAsPublished(Long meetingId);

	List<MeetingPaymentOutbox> getUnpublishedPaymentOutbox();
}
