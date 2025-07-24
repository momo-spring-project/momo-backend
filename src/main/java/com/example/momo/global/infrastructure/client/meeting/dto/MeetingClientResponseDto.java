package com.example.momo.global.infrastructure.client.meeting.dto;

import java.time.LocalDateTime;

import com.example.momo.domain.meeting.enums.MeetingStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeetingClientResponseDto {

	private final Long id;
	private final Long hostUserId;
	private final String title;
	private final String description;
	private final Integer categoryId;
	private final int currentParticipantsCount;
	private final int maxParticipantsCount;
	private final LocalDateTime meetingDate;
	private final LocalDateTime meetingEndDate;
	private final String locationName;
	private final Double latitude;
	private final Double longitude;
	private final Double minEnterScore;
	private final int participationFee;
	private final MeetingStatus status;
}
