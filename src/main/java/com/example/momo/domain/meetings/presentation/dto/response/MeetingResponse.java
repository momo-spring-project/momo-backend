package com.example.momo.domain.meetings.presentation.dto.response;

import java.time.LocalDateTime;

import com.example.momo.domain.meetings.domain.Meeting;
import com.example.momo.domain.meetings.enums.MeetingStatus;

import lombok.Getter;

@Getter
public class MeetingResponse {

	private final Long id;
	private final Long hostUserId;
	private final String title;
	private final String description;
	private final int categoryId;
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
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;

	public MeetingResponse(Meeting meeting) {
		this.id = meeting.getId();
		this.hostUserId = meeting.getHostUserId();
		this.title = meeting.getTitle();
		this.description = meeting.getDescription();
		this.categoryId = meeting.getCategoryId();
		this.currentParticipantsCount = meeting.getCurrentParticipantsCount();
		this.maxParticipantsCount = meeting.getMaxParticipantsCount();
		this.meetingDate = meeting.getMeetingDate();
		this.meetingEndDate = meeting.getMeetingEndDate();
		this.locationName = meeting.getLocationName();
		this.latitude = meeting.getLatitude();
		this.longitude = meeting.getLongitude();
		this.minEnterScore = meeting.getMinEnterScore();
		this.participationFee = meeting.getParticipationFee();
		this.status = meeting.getStatus();
		this.createdAt = meeting.getCreatedAt();
		this.updatedAt = meeting.getUpdatedAt();
	}
}
