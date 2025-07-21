package com.example.momo.domain.meetings.domain;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.momo.domain.meetings.enums.MeetingStatus;

public interface MeetingRepository {

	Optional<Meeting> findById(Long meetingId);

	Meeting save(Meeting meeting);

	Page<Meeting> findMeetings(String title, LocalDateTime meetingDate, MeetingStatus status,
		Pageable pageable);

	boolean existsById(Long id);
}
