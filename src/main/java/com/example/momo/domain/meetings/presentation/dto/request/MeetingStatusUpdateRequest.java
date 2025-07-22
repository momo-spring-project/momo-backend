package com.example.momo.domain.meetings.presentation.dto.request;

import com.example.momo.domain.meetings.enums.MeetingStatus;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class MeetingStatusUpdateRequest {

	@NotBlank
	private MeetingStatus status;
}
