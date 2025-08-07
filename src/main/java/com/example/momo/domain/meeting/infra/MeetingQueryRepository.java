package com.example.momo.domain.meeting.infra;

import java.time.LocalDateTime;
import java.util.List;

import com.example.momo.domain.meeting.domain.Meeting;

public interface MeetingQueryRepository {

	List<Meeting> findMeetingsByUserId(Long userId);

	Long countParticipants(Long userId, Long meetingId, Boolean attendance, LocalDateTime createdAt);
}
