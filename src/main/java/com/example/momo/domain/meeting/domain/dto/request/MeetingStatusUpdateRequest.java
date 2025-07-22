package com.example.momo.domain.meeting.domain.dto.request;

import com.example.momo.domain.meeting.enums.MeetingStatus;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class MeetingStatusUpdateRequest {

	@NotBlank(message = "상태값은 필수입니다.")
	private MeetingStatus status;
}
