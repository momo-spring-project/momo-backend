package com.example.momo.domain.meeting.application;

import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.domain.MeetingParticipant;
import com.example.momo.domain.meeting.domain.MeetingRepository;
import com.example.momo.domain.meeting.exception.MeetingException;
import com.example.momo.domain.meeting.exception.MeetingExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MeetingReader {

	private final MeetingRepository meetingRepository;

	// 모임 조회
	public Meeting getMeetingById(Long meetingId) {

		Meeting meeting = meetingRepository.findById(meetingId)
			.orElseThrow(() -> new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND));
		if(meeting.getIsDeleted()){
			throw new MeetingException(MeetingExceptionCode.DELETED_MEETING);
		}

		return meeting;
	}

	// 참가자 조회
	public MeetingParticipant getParticipantByMeetingIdAndUserId(Long meetingId, Long userId) {

		return meetingRepository.findByMeetingIdAndUserId(meetingId, userId)
			.orElseThrow(() -> new MeetingException(MeetingExceptionCode.PARTICIPANT_NOT_FOUND));
	}

	public MeetingParticipant getParticipantById(Long participantId) {
		return meetingRepository.findParticipantById(participantId)
			.orElseThrow(() -> new MeetingException(MeetingExceptionCode.PARTICIPANT_NOT_FOUND));
	}
}
