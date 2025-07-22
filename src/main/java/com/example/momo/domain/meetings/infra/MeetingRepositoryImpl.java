package com.example.momo.domain.meetings.infra;

import java.time.LocalDateTime;
import java.util.Optional;

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
}
