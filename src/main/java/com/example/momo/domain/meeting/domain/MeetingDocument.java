package com.example.momo.domain.meeting.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.example.momo.domain.meeting.enums.MeetingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Document(indexName = "meetings")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingDocument {

	@Id
	private String id;
	private Long hostUserId;
	private String title;
	private String description;
	private Integer categoryId;
	private Integer currentParticipantsCount;
	private Integer maxParticipantsCount;

	@Field(type = FieldType.Date, format = DateFormat.date_hour_minute)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime meetingDate;

	@Field(type = FieldType.Date, format = DateFormat.date_hour_minute)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime meetingEndDate;

	@Field(type = FieldType.Date, format = DateFormat.date_hour_minute)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime createdAt;

	@Field(type = FieldType.Date, format = DateFormat.date_hour_minute)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime updatedAt;

	private String locationName;
	private Double latitude;
	private Double longitude;
	private Double minEnterScore;
	private int participationFee;

	@Field(type = FieldType.Keyword)
	private MeetingStatus status;

	public static MeetingDocument from(Meeting meeting) {
		return MeetingDocument.builder()
			.id(meeting.getId().toString())
			.hostUserId(meeting.getHostUserId())
			.title(meeting.getTitle())
			.description(meeting.getDescription())
			.categoryId(meeting.getCategoryId())
			.currentParticipantsCount(meeting.getCurrentParticipantsCount())
			.maxParticipantsCount(meeting.getMaxParticipantsCount())
			.meetingDate(meeting.getMeetingDate())
			.meetingEndDate(meeting.getMeetingEndDate())
			.locationName(meeting.getLocationName())
			.latitude(meeting.getLatitude())
			.longitude(meeting.getLongitude())
			.minEnterScore(meeting.getMinEnterScore())
			.participationFee(meeting.getParticipationFee())
			.status(meeting.getStatus())
			.createdAt(meeting.getCreatedAt())
			.updatedAt(meeting.getUpdatedAt())
			.build();
	}
}
