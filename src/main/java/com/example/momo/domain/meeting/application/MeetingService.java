package com.example.momo.domain.meeting.application;

import java.time.LocalDateTime;
import java.util.List;

import com.example.momo.domain.meeting.application.dto.request.MeetingCreateRequestDto;
import com.example.momo.domain.meeting.application.dto.request.MeetingUpdateRequestDto;
import com.example.momo.domain.meeting.application.dto.response.MeetingPagingResponseDto;
import com.example.momo.domain.meeting.application.dto.response.MeetingResponseDto;
import com.example.momo.domain.meeting.application.dto.response.ParticipantCountResponseDto;
import com.example.momo.domain.meeting.application.dto.response.ParticipantCreateResponseDto;
import com.example.momo.domain.meeting.application.dto.response.ParticipantResponseDto;
import com.example.momo.domain.meeting.domain.MeetingDocument;
import com.example.momo.domain.meeting.domain.MeetingParticipant;
import com.example.momo.domain.meeting.enums.MeetingStatus;

public interface MeetingService {

	/** Meeting Service */

	MeetingResponseDto createMeeting(MeetingCreateRequestDto request, Long userId);

	MeetingResponseDto updateMeeting(MeetingUpdateRequestDto request, Long meetingId, Long userId);

	MeetingResponseDto getMeeting(Long meetingId);

	MeetingResponseDto updateMeetingStatus(Long meetingId, MeetingStatus status, Long userId);

	MeetingPagingResponseDto<MeetingDocument> getMeetings(String title, MeetingStatus status,
		LocalDateTime meetingDate, Integer categoryId,
		int page, int size);

	void deleteMeeting(Long meetingId, Long userId);

	List<MeetingResponseDto> getMeetingsByUserId(Long userId);

	/** Meeting Participant Service */

	ParticipantCreateResponseDto createParticipant(Long userId, Long meetingId);

	List<ParticipantResponseDto> getParticipants(Long meetingId);

	ParticipantResponseDto deleteParticipant(Long userId, Long meetingId);

	ParticipantResponseDto updateParticipantStatus(Long id, Long meetingId, double lat, double lng);

	ParticipantCountResponseDto getParticipantCount(Long userId, Long meetingId, Boolean attendance,
		LocalDateTime createdAt);

}
