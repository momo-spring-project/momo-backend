package com.example.momo.domain.meetings.infra;

import com.example.momo.domain.meetings.domain.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingParticipantJpaRepository extends JpaRepository<MeetingParticipant, Long> {
}
