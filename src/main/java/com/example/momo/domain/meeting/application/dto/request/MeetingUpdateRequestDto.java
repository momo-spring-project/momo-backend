package com.example.momo.domain.meeting.application.dto.request;

import java.time.LocalDateTime;

import com.example.momo.domain.meeting.enums.MeetingStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MeetingUpdateRequestDto(
	@NotBlank String title,
	@NotBlank String description,
	Integer categoryId,
	@NotNull int maxParticipantsCount,
	@NotNull LocalDateTime meetingDate,
	@NotNull LocalDateTime meetingEndDate,
	@NotBlank String locationName,
	@NotNull Double latitude,
	@NotNull Double longitude,
	Double minEnterScore,
	int participationFee,
	@NotNull MeetingStatus status
) {
}