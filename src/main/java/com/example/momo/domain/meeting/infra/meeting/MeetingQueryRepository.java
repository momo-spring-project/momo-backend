package com.example.momo.domain.meeting.infra.meeting;

import java.util.List;

import com.example.momo.domain.meeting.domain.Meeting;

public interface MeetingQueryRepository {

	List<Meeting> findMeetingsByUserId(Long userId);
}
