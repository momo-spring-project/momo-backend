package com.example.momo.domain.meetings.infra;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.momo.domain.meetings.domain.Meeting;
import com.example.momo.domain.meetings.domain.MeetingRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MeetingRepositoryImpl implements MeetingRepository {

	private final MeetingJpaRepository meetingJpaRepository;

	@Override
	public Optional<Meeting> findById(Long id) {
		return meetingJpaRepository.findById(id);
	}

	@Override
	public Meeting save(Meeting meeting) {
		return meetingJpaRepository.save(meeting);
	}
}
