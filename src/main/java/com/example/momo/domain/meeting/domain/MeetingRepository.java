package com.example.momo.domain.meeting.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.momo.domain.meeting.enums.MeetingStatus;

public interface MeetingRepository {

	/* Meeting Repository */

	Optional<Meeting> findById(Long meetingId);

	Meeting save(Meeting meeting);

	Page<MeetingDocument> getMeetings(String title, LocalDateTime meetingDate, MeetingStatus status,
		Integer categoryId, Pageable pageable);

	Page<Meeting> getMeetingsForDatabase(String title, LocalDateTime meetingDate, MeetingStatus status,
		Integer categoryId,
		Pageable pageable);

	List<Meeting> findMeetingsByUserId(Long userId);

	void saveMeetingElastic(Meeting meeting);

	void deleteMeetingElastic(Meeting meeting);

	boolean existsById(Long id);

	/* Meeting Participant Repository */

	Long countParticipants(Long userId, Long meetingId, Boolean attendance, LocalDateTime createdAt);

}
