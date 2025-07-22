package com.example.momo.domain.meetings.presentation.dto.request;

import java.time.LocalDateTime;

import com.example.momo.domain.meetings.enums.MeetingStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class MeetingUpdateRequest {

	@NotBlank
	private String title;

	@NotBlank
	private String description;

	private Integer categoryId;

	@NotNull
	private int maxParticipantsCount;

	@NotNull
	private LocalDateTime meetingDate;

	@NotNull
	private LocalDateTime meetingEndDate;

	@NotBlank
	private String locationName;

	@NotNull
	private Double latitude;

	@NotNull
	private Double longitude;

	private Double minEnterScore;

	private int participationFee;

	@NotNull
	private MeetingStatus status;
}
