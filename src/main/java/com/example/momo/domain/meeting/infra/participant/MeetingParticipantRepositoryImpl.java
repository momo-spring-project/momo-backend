package com.example.momo.domain.meeting.infra.participant;

import com.example.momo.domain.meeting.domain.MeetingParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MeetingParticipantRepositoryImpl implements MeetingParticipantRepository {

	private final MeetingParticipantJpaRepository meetingParticipantJpaRepository;

	@Override
	public boolean existsByMeetingIdAndUserId(Long meetingId, Long userId) {
		return meetingParticipantJpaRepository.existsByMeetingIdAndUserId(meetingId, userId);
	}

	@Override
	public MeetingParticipant save(MeetingParticipant meetingParticipant) {
		return meetingParticipantJpaRepository.save(meetingParticipant);
	}

	@Override
	public List<Long> findParticipantsIdsByMeetingId(Long meetingId) {
		return meetingParticipantJpaRepository.findParticipantsIdByMeetingId(meetingId);
	}

	@Override
	public Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId) {
		return meetingParticipantJpaRepository.findByMeetingIdAndUserId(meetingId, userId);
	}
}