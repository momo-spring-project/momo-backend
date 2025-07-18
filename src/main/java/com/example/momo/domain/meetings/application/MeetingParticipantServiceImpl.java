package com.example.momo.domain.meetings.application;

import com.example.momo.domain.meetings.domain.Meeting;
import com.example.momo.domain.meetings.domain.MeetingParticipant;
import com.example.momo.domain.meetings.domain.MeetingRepository;
import com.example.momo.domain.meetings.exception.MeetingException;
import com.example.momo.domain.meetings.exception.MeetingExceptionCode;
import com.example.momo.domain.meetings.presentation.dto.ParticipantAddResponseDto;
import com.example.momo.domain.meetings.domain.MeetingParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeetingParticipantServiceImpl implements MeetingParticipantService {

	private final MeetingRepository meetingRepository;
	private final MeetingParticipantRepository meetingParticipantRepository;

	@Override
	@Transactional
	public ParticipantAddResponseDto addParticipant(Long userId, Long meetingId) {
		Meeting meeting = meetingRepository.findById(meetingId)
			.orElseThrow(() -> new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND));

		MeetingParticipant participant = new MeetingParticipant(meetingId, userId);

		MeetingParticipant savedParticipant = meetingParticipantRepository.save(participant);

		return new ParticipantAddResponseDto(savedParticipant);
	}
}
