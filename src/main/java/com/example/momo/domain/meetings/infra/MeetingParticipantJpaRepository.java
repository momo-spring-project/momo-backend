package com.example.momo.domain.meetings.infra;

import com.example.momo.domain.meetings.domain.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MeetingParticipantJpaRepository extends JpaRepository<MeetingParticipant, Long> {
	boolean existsByMeetingIdAndUserId(Long meetingId, Long userId);

	@Query("SELECT mp.userId FROM MeetingParticipant mp WHERE mp.meetingId = :meetingId")
	List<Long> findParticipantsIdByMeetingId(@Param("meetingId") Long meetingId);

	Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);
}
