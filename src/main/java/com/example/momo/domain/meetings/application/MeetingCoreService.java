package com.example.momo.domain.meetings.application;

import com.example.momo.domain.meetings.enums.MeetingStatus;
import com.example.momo.domain.meetings.presentation.dto.request.MeetingCreateRequest;
import com.example.momo.domain.meetings.presentation.dto.request.MeetingUpdateRequest;
import com.example.momo.domain.meetings.presentation.dto.response.MeetingPagingResponse;
import com.example.momo.domain.meetings.presentation.dto.response.MeetingResponse;

public interface MeetingCoreService {

	MeetingResponse createMeeting(MeetingCreateRequest request, Long userId);

	MeetingResponse updateMeeting(MeetingUpdateRequest request, Long meetingId, Long userId);

	MeetingResponse searchMeeting(Long meetingId);

	MeetingResponse updateMeetingStatus(Long meetingId, MeetingStatus status, Long userId);

	MeetingPagingResponse<MeetingResponse> getMeetings(String title, int page, int size);

	void deleteMeeting(Long meetingId, Long userId);
}
