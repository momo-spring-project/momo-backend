package com.example.momo.domain.meetings.entity;

import java.time.LocalDateTime;

import com.example.momo.domain.common.entity.BaseEntity;
import com.example.momo.domain.meetings.enums.MeetingStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "meetings")
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meeting extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, name = "host_user_id")
	private Long hostUserId;

	@Column(nullable = false, name = "title")
	private String title;

	@Column(nullable = false, name = "description")
	private String description;

	@Column(name = "category_id")
	private Long categoryId;

	@Column(nullable = false, name = "current_participants_count")
	private int currentParticipantsCount;

	@Column(nullable = false, name = "max_participants_count")
	private int maxParticipantsCount;

	@Column(nullable = false, name = "meeting_date")
	private LocalDateTime meetingDate;

	@Column(nullable = false, name = "meeting_end_date")
	private LocalDateTime meetingEndDate;

	@Column(nullable = false, name = "location_name")
	private String locationName;

	@Column(nullable = false, name = "latitude")
	private Double latitude;

	@Column(nullable = false, name = "longitude")
	private Double longitude;

	@Column(nullable = false, name = "participation_fee")
	private int participationFee = 0;

	// TODO : 추후 Converter 변경 고려
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "status")
	private MeetingStatus status; // 모집중, 모집완료
}
