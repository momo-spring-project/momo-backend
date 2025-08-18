package com.example.momo.domain.meeting.infra;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.domain.MeetingDocument;
import com.example.momo.domain.meeting.domain.MeetingRepository;
import com.example.momo.domain.meeting.enums.MeetingStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MeetingRepositoryImpl implements MeetingRepository {

	private final MeetingJpaRepository meetingJpaRepository;
	private final MeetingQueryRepository meetingQueryRepository;
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
	public Page<Meeting> getMeetingsForDatabase(String title, LocalDateTime meetingDate, MeetingStatus status,
		Integer categoryId, Pageable pageable) {
		
		return meetingQueryRepository.getMeetings(title, meetingDate, status, categoryId, pageable);
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

	@Override
	public List<Meeting> findMeetingsByUserId(Long userId) {
		return meetingQueryRepository.findMeetingsByUserId(userId);
	}

	/* Meeting Participant Repository */

	@Override
	public Long countParticipants(Long userId, Long meetingId, Boolean attendance, LocalDateTime createdAt) {
		return meetingQueryRepository.countParticipants(userId, meetingId, attendance, createdAt);
	}
}
