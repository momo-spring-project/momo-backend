package com.example.momo.domain.meeting.infra.meeting;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.domain.MeetingDocument;
import com.example.momo.domain.meeting.domain.MeetingParticipant;
import com.example.momo.domain.meeting.domain.MeetingRepository;
import com.example.momo.domain.meeting.enums.MeetingStatus;
import com.example.momo.domain.meeting.infra.participant.MeetingParticipantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MeetingRepositoryImpl implements MeetingRepository {

	private final MeetingJpaRepository meetingJpaRepository;
	private final MeetingQueryRepository meetingQueryRepository;
	private final MeetingParticipantRepository meetingParticipantRepository;
	private final MeetingElasticCustomRepository meetingElasticCustomRepository;
	private final MeetingElasticRepository meetingElasticRepository;
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
	public Page<MeetingDocument> getMeetings(String title, LocalDateTime meetingDate, MeetingStatus status,
		Integer categoryId, Pageable pageable) {

		return meetingElasticCustomRepository.getMeetings(title, meetingDate, status, categoryId, pageable);
	}

	@Override
	public void saveMeetingElastic(Meeting meeting) {

		meetingElasticRepository.save(MeetingDocument.from(meeting));
	}

	@Override
	public void deleteMeetingElastic(Meeting meeting) {

		meetingElasticRepository.delete(MeetingDocument.from(meeting));
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
	public List<MeetingParticipant> findAllParticipantsByMeetingId(Long meetingId) {
		return meetingParticipantRepository.findAllParticipantsByMeetingId(meetingId);
	}

	@Override
	public Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId) {
		return meetingParticipantRepository.findByMeetingIdAndUserId(meetingId, userId);
	}

	@Override
	public Optional<MeetingParticipant> findParticipantById(Long participantId) {
		return meetingParticipantRepository.findById(participantId);
	}

	@Override
	public Long countParticipants(Long userId, Long meetingId, Boolean attendance, LocalDateTime createdAt) {
		return meetingParticipantRepository.countParticipants(userId, meetingId, attendance, createdAt);
	}

	@Override
	public List<Meeting> findMeetingsByUserId(Long userId) {
		return meetingQueryRepository.findMeetingsByUserId(userId);
	}

}
