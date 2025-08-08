package com.example.momo.domain.meeting.infra;

import com.example.momo.domain.meeting.domain.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MeetingParticipantJpaRepository extends JpaRepository<MeetingParticipant, Long> {

	@Modifying
	@Query("DELETE FROM MeetingParticipant mp WHERE mp.id = :participantId")
	void removeMeetingParticipantById(Long participantId);
}
