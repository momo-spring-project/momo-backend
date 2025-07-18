package com.example.momo.domain.meetings.infra;

import org.springframework.stereotype.Repository;

import com.example.momo.domain.meetings.domain.MeetingRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MeetingRepositoryImpl implements MeetingRepository {

	private final MeetingJpaRepository meetingJpaRepository;
}
