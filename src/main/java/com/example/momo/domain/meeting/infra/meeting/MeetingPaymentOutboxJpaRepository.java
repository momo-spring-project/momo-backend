package com.example.momo.domain.meeting.infra.meeting;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.momo.domain.meeting.domain.MeetingPaymentOutbox;

public interface MeetingPaymentOutboxJpaRepository extends JpaRepository<MeetingPaymentOutbox, Long> {

	List<MeetingPaymentOutbox> findByPublishedFalseOrderByCreatedAt();

	Optional<MeetingPaymentOutbox> findByMeetingId(Long meetingId);
}
