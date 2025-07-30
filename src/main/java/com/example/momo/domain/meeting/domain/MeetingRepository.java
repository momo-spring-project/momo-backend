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

	Page<Meeting> getMeetings(String title, LocalDateTime meetingDate, MeetingStatus status, Integer categoryId,
		Pageable pageable);

	Page<MeetingDocument> getMeetingDocuments(String title, LocalDateTime meetingDate, MeetingStatus status,
		Integer categoryId, Pageable pageable);

	List<Meeting> findMeetingsByUserId(Long userId);

	boolean existsById(Long id);

	/* Meeting Participant Repository */

	boolean existsByMeetingIdAndUserId(Long meetingId, Long userId);

	MeetingParticipant saveParticipant(MeetingParticipant meetingParticipant);

	List<MeetingParticipant> findAllParticipantsByMeetingId(Long meetingId);

	Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);

	Optional<MeetingParticipant> findParticipantById(Long participantId);

	Long countParticipants(Long userId, Long meetingId, Boolean attendance, LocalDateTime createdAt);

}
