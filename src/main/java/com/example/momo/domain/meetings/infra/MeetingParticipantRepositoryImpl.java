package com.example.momo.domain.meetings.infra;

import com.example.momo.domain.meetings.domain.MeetingParticipant;
import com.example.momo.domain.meetings.domain.MeetingParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MeetingParticipantRepositoryImpl implements MeetingParticipantRepository {

	private final MeetingParticipantJpaRepository meetingParticipantJpaRepository;

	@Override
	public MeetingParticipant save(MeetingParticipant meetingParticipant) {
		return meetingParticipantJpaRepository.save(meetingParticipant);
	}
}