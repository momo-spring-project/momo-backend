package com.example.momo.domain.meeting.domain.dto.request;

import com.example.momo.domain.meeting.enums.MeetingStatus;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class MeetingStatusUpdateRequestDto {

	@NotBlank
	private MeetingStatus status;
}
