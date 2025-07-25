package com.example.momo.domain.meeting.infra.participant;

import com.example.momo.domain.meeting.domain.MeetingParticipant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetingParticipantRepository {
	boolean existsByMeetingIdAndUserId(Long meetingId, Long userId);

	MeetingParticipant save(MeetingParticipant meetingParticipant);

	List<MeetingParticipant> findAllParticipantsByMeetingId(Long meetingId);

	Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);

	Optional<MeetingParticipant> findById(Long id);

	Long countParticipants(Long userId, Long meetingId, Boolean attendance, LocalDateTime createdAt);
}
