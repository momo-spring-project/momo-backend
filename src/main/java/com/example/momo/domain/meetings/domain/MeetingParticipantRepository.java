package com.example.momo.domain.meetings.domain;

import java.util.List;
import java.util.Optional;

public interface MeetingParticipantRepository {
	boolean existsByMeetingIdAndUserId(Long meetingId, Long userId);

	MeetingParticipant save(MeetingParticipant meetingParticipant);

	List<Long> findParticipantsIdsByMeetingId(Long meetingId);

	Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);
}
