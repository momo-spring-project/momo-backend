package com.example.momo.domain.meetings.domain;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MeetingRepository {

	Optional<Meeting> findById(Long meetingId);

	Meeting save(Meeting meeting);

	Page<Meeting> findAllByTitleContaining(String title, Pageable pageable);
}
