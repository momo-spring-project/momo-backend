package com.example.momo.domain.meeting.application.dto.request;

import com.example.momo.domain.meeting.enums.MeetingStatus;

import jakarta.validation.constraints.NotBlank;

public record MeetingStatusUpdateRequestDto(
	@NotBlank
	MeetingStatus status
) {
}
