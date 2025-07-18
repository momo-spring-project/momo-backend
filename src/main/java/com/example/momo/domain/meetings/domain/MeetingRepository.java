package com.example.momo.domain.meetings.domain;

import java.util.Optional;

public interface MeetingRepository {
	Optional<Meeting> findById(Long id);
}
