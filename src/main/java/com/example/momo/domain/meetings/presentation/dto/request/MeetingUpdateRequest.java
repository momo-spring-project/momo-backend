package com.example.momo.domain.meetings.presentation.dto.request;

import java.time.LocalDateTime;

import com.example.momo.domain.meetings.enums.MeetingStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class MeetingUpdateRequest {

	@NotBlank(message = "제목은 필수 사항입니다.")
	private String title;

	@NotBlank(message = "설명은 필수 사항입니다.")
	private String description;

	private int categoryId;

	@NotNull(message = "최대 참여자수는 필수 사항입니다.")
	private int maxParticipantsCount;

	@NotNull(message = "모임 날짜는 필수 사항입니다.")
	private LocalDateTime meetingDate;

	@NotNull(message = "모임 마감 날짜는 필수 사항입니다.")
	private LocalDateTime meetingEndDate;

	@NotBlank(message = "모임 위치는 필수 사항입니다.")
	private String LocationName;

	@NotNull(message = "위도는 필수 사항입니다.")
	private Double latitude;

	@NotNull(message = "경도는 필수 사항입니다.")
	private Double longitude;

	private Double minEnterScore;

	private int participationFee;

	@NotNull(message = "상태는 필수 사항입니다.")
	private MeetingStatus status;
}
