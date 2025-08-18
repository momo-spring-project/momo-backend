package com.example.momo.domain.meeting.application.dto.response;

import java.time.LocalDateTime;

import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.enums.MeetingStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MeetingResponseDto{

	private Long id;
	private Long hostUserId;
	private String title;
	private String description;
	private Integer categoryId;
	private int currentParticipantsCount;
	private int maxParticipantsCount;
	private LocalDateTime meetingDate;
	private LocalDateTime meetingEndDate;
	private String locationName;
	private Double latitude;
	private Double longitude;
	private Double minEnterScore;
	private int participationFee;
	private MeetingStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public MeetingResponseDto(Meeting meeting) {
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
