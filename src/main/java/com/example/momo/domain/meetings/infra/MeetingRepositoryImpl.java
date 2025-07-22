package com.example.momo.domain.meetings.infra;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.momo.domain.meetings.domain.MeetingParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.meetings.domain.Meeting;
import com.example.momo.domain.meetings.domain.MeetingRepository;
import com.example.momo.domain.meetings.enums.MeetingStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MeetingRepositoryImpl implements MeetingRepository {

	private final MeetingJpaRepository meetingJpaRepository;
	private final MeetingQueryRepository meetingQueryRepository;
	private final MeetingParticipantRepository meetingParticipantRepository;

	/* Meeting Repository */

	@Override
	public Optional<Meeting> findById(Long id) {
		return meetingJpaRepository.findByIdAndIsDeletedFalse(id);
	}

	@Override
	public Meeting save(Meeting meeting) {
		return meetingJpaRepository.save(meeting);
	}

	@Override
	public Page<Meeting> findMeetings(String title, LocalDateTime meetingDate, MeetingStatus status,
		Pageable pageable) {

		return meetingQueryRepository.findMeetings(title, meetingDate, status, pageable);
	}

	@Override
	public boolean existsById(Long id) {
		return meetingJpaRepository.existsById(id);
	}

	/* Meeting Participant Repository */

	@Override
	public boolean existsByMeetingIdAndUserId(Long meetingId, Long userId) {
		return meetingParticipantRepository.existsByMeetingIdAndUserId(meetingId, userId);
	}

	@Override
	public MeetingParticipant saveParticipant(MeetingParticipant meetingParticipant) {
		return meetingParticipantRepository.save(meetingParticipant);
	}

	@Override
	public List<Long> findParticipantsIdsByMeetingId(Long meetingId) {
		return meetingParticipantRepository.findParticipantsIdsByMeetingId(meetingId);
	}

	@Override
	public Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId) {
		return meetingParticipantRepository.findByMeetingIdAndUserId(meetingId, userId);
	}
}
