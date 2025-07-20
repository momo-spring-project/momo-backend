package com.example.momo.domain.meetings.presentation.dto.request;

import com.example.momo.domain.meetings.enums.MeetingStatus;

import lombok.Getter;

@Getter
public class MeetingStatusUpdateRequest {

	private MeetingStatus status;
}
