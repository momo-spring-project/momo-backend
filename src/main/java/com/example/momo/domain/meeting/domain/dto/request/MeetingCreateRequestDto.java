package com.example.momo.domain.meeting.domain.dto.request;

import java.time.LocalDateTime;
import java.util.Optional;

import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.enums.MeetingStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class MeetingCreateRequestDto {

	@NotBlank
	private String title;

	@NotBlank
	private String description;

	private Integer categoryId;

	@NotNull
	private Integer maxParticipantsCount;

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

	public Meeting toMeeting(Long userId) {
		return Meeting.builder()
			.hostUserId(userId)
			.title(this.title)
			.description(this.description)
			.categoryId(this.categoryId)
			.currentParticipantsCount(1) // 기본값 1로 설정
			.maxParticipantsCount(this.maxParticipantsCount)
			.meetingDate(this.meetingDate)
			.meetingEndDate(this.meetingEndDate)
			.locationName(this.locationName)
			.latitude(this.latitude)
			.longitude(this.longitude)
			.minEnterScore(Optional.ofNullable(this.minEnterScore).orElse(0.0))
			.participationFee(this.participationFee)
			.status(MeetingStatus.IN_PROGRESS)
			.build();
	}
}
