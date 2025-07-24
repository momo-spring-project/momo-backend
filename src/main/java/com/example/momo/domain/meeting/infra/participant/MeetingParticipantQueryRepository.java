package com.example.momo.domain.meeting.infra.participant;

import java.time.LocalDateTime;

public interface MeetingParticipantQueryRepository {
	Long countParticipants(Long meetingId, Boolean attendance, LocalDateTime createdAt);
}
