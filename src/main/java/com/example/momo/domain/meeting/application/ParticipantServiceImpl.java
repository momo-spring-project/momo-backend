package com.example.momo.domain.meeting.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.meeting.application.dto.response.ParticipantResponseDto;
import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.domain.MeetingParticipant;
import com.example.momo.domain.meeting.domain.MeetingRepository;
import com.example.momo.domain.meeting.exception.MeetingException;
import com.example.momo.domain.meeting.exception.MeetingExceptionCode;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

	private final MeetingReader meetingReader;
	private final MeetingRepository meetingRepository;
	private final EntityManager entityManager;

	@Override
	@Transactional
	public ParticipantResponseDto addParticipant(Long meetingId, Long userId) {

		Meeting meeting = meetingReader.getMeetingById(meetingId);

		if (meeting.getCurrentParticipantsCount() >= meeting.getMaxParticipantsCount()) {
			throw new MeetingException(MeetingExceptionCode.MEETING_IS_FULL);
		}

		// 참가자 추가
		MeetingParticipant participant = new MeetingParticipant(meeting.getId(), userId);

		meeting.addMeetingParticipant();
		MeetingParticipant savedParticipant = meetingRepository.saveParticipant(participant);

		return new ParticipantResponseDto(savedParticipant);
	}

	@Override
	@Transactional
	public ParticipantResponseDto removeParticipant(Long meetingId, MeetingParticipant participant) {

		Meeting meeting = meetingReader.getMeetingById(meetingId);

		if (meeting.getCurrentParticipantsCount() <= 0) {
			throw new MeetingException(MeetingExceptionCode.INVALID_PARTICIPANT_COUNT);
		}

		// 인원 계산, 참가자 삭제
		entityManager.remove(participant);
		meeting.removeMeetingParticipant();

		return new ParticipantResponseDto(participant);
	}
}
