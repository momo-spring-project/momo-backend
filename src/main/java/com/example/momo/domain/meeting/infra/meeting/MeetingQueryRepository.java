package com.example.momo.domain.meeting.infra.meeting;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.enums.MeetingStatus;

public interface MeetingQueryRepository {

	Page<Meeting> findMeetings(String title, LocalDateTime meetingDate, MeetingStatus status, Integer categoryId,
		Pageable pageable);

	List<Meeting> findMeetingsByUserId(Long userId);
}
