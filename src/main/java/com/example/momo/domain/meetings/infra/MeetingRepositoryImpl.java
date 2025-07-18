package com.example.momo.domain.meetings.infra;

import com.example.momo.domain.meetings.domain.Meeting;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.meetings.domain.MeetingRepository;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MeetingRepositoryImpl implements MeetingRepository {

	private final MeetingJpaRepository meetingJpaRepository;

	@Override
	public Optional<Meeting> findById(Long id) {
		return meetingJpaRepository.findById(id);
	}
}
