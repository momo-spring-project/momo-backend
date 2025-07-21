package com.example.momo.domain.meetings.presentation.dto.request;

import java.time.LocalDateTime;
import java.util.Optional;

import com.example.momo.domain.meetings.domain.Meeting;
import com.example.momo.domain.meetings.enums.MeetingStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class MeetingCreateRequest {

	@NotBlank(message = "제목은 필수 사항입니다.")
	private String title;

	@NotBlank(message = "설명은 필수 사항입니다.")
	private String description;

	private Integer categoryId;

	@NotNull(message = "최대 참여자수는 필수 사항입니다.")
	private Integer maxParticipantsCount;

	@NotNull(message = "모임 날짜는 필수 사항입니다.")
	private LocalDateTime meetingDate;

	@NotNull(message = "모임 마감 날짜는 필수 사항입니다.")
	private LocalDateTime meetingEndDate;

	@NotBlank(message = "모임 위치는 필수 사항입니다.")
	private String locationName;

	@NotNull(message = "위도는 필수 사항입니다.")
	private Double latitude;

	@NotNull(message = "경도는 필수 사항입니다.")
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
