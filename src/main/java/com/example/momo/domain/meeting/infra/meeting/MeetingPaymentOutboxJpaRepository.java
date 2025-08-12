package com.example.momo.domain.meeting.infra.meeting;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.momo.domain.meeting.domain.MeetingPaymentOutbox;
import org.springframework.data.jpa.repository.Query;

public interface MeetingPaymentOutboxJpaRepository extends JpaRepository<MeetingPaymentOutbox, Long> {

	List<MeetingPaymentOutbox> findByPublishedFalseOrderByCreatedAt();

	Optional<MeetingPaymentOutbox> findByMeetingId(Long meetingId);

	Optional<MeetingPaymentOutbox> findByEventUuid(String eventUuid);

	@Query("SELECT mpo FROM MeetingPaymentOutbox mpo WHERE mpo.published AND NOT mpo.processed")
	List<MeetingPaymentOutbox> findByPublishedTrueAndProcessedFalse();
}
