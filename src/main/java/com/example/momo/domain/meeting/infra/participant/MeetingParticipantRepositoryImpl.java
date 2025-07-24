package com.example.momo.domain.meeting.infra.participant;

import com.example.momo.domain.meeting.domain.MeetingParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MeetingParticipantRepositoryImpl implements MeetingParticipantRepository {

	private final MeetingParticipantJpaRepository meetingParticipantJpaRepository;
	private final MeetingParticipantQueryRepository meetingParticipantQueryRepository;

	@Override
	public boolean existsByMeetingIdAndUserId(Long meetingId, Long userId) {
		return meetingParticipantJpaRepository.existsByMeetingIdAndUserId(meetingId, userId);
	}

	@Override
	public MeetingParticipant save(MeetingParticipant meetingParticipant) {
		return meetingParticipantJpaRepository.save(meetingParticipant);
	}

	@Override
	public List<MeetingParticipant> findAllParticipantsByMeetingId(Long meetingId) {
		return meetingParticipantJpaRepository.findAllByMeetingId(meetingId);
	}

	@Override
	public Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId) {
		return meetingParticipantJpaRepository.findByMeetingIdAndUserId(meetingId, userId);
	}

	@Override
	public Optional<MeetingParticipant> findById(Long id) {
		return meetingParticipantJpaRepository.findById(id);
	}

	@Override
	public Long countParticipants(Long meetingId, Boolean attendance, LocalDateTime createdAt) {
		return meetingParticipantQueryRepository.countParticipants(meetingId, attendance, createdAt);
	}
}