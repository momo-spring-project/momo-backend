package com.example.momo.domain.meetings.infra;

import com.example.momo.domain.meetings.domain.MeetingParticipant;

import java.util.List;
import java.util.Optional;

public interface MeetingParticipantRepository {
	boolean existsByMeetingIdAndUserId(Long meetingId, Long userId);

	MeetingParticipant save(MeetingParticipant meetingParticipant);

	List<Long> findParticipantsIdsByMeetingId(Long meetingId);

	Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);
}
