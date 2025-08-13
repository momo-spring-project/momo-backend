package com.example.momo.domain.meeting.application.dto.request;

import java.time.LocalDateTime;
import java.util.Optional;

import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.enums.MeetingStatus;

public record MeetingCreateRequestDto(
	String title,
	String description,
	Integer categoryId,
	Integer maxParticipantsCount,
	LocalDateTime meetingDate,
	LocalDateTime meetingEndDate,
	String locationName,
	Double latitude,
	Double longitude,
	Double minEnterScore,
	int participationFee
) {
	public Meeting toMeeting(Long userId) {
		return Meeting.create(
			userId,
			title,
			description,
			categoryId,
			1,
			maxParticipantsCount,
			meetingDate,
			meetingEndDate,
			locationName,
			latitude,
			longitude,
			Optional.ofNullable(minEnterScore).orElse(0.0),
			participationFee,
			MeetingStatus.IN_PROGRESS
		);
	}
}