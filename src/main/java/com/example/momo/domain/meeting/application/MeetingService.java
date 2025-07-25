package com.example.momo.domain.meeting.application;

import java.time.LocalDateTime;
import java.util.List;

import com.example.momo.domain.meeting.domain.dto.request.MeetingCreateRequestDto;
import com.example.momo.domain.meeting.domain.dto.request.MeetingUpdateRequestDto;
import com.example.momo.domain.meeting.domain.dto.response.MeetingPagingResponseDto;
import com.example.momo.domain.meeting.domain.dto.response.MeetingResponseDto;
import com.example.momo.domain.meeting.domain.dto.response.ParticipantResponseDto;
import com.example.momo.domain.meeting.enums.MeetingStatus;

public interface MeetingService {

	/** Meeting Service */

	MeetingResponseDto createMeeting(MeetingCreateRequestDto request, Long userId);

	MeetingResponseDto updateMeeting(MeetingUpdateRequestDto request, Long meetingId, Long userId);

	MeetingResponseDto getMeeting(Long meetingId);

	MeetingResponseDto updateMeetingStatus(Long meetingId, MeetingStatus status, Long userId);

	MeetingPagingResponseDto<MeetingResponseDto> getMeetings(String title, MeetingStatus status,
		LocalDateTime meetingDate, Integer categoryId,
		int page, int size);

	void deleteMeeting(Long meetingId, Long userId);

	/** Meeting Participant Service */

	ParticipantResponseDto createParticipant(Long userId, Long meetingId);

	ParticipantResponseDto getParticipant(Long participantId);

	List<Long> getParticipants(Long meetingId);

	ParticipantResponseDto deleteParticipant(Long userId, Long meetingId);

	ParticipantResponseDto updateParticipantStatus(Long id, Long meetingId, double lat, double lng);
}
