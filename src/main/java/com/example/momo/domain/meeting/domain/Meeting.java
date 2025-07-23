package com.example.momo.domain.meeting.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.momo.domain.meeting.domain.dto.request.MeetingUpdateRequestDto;
import com.example.momo.domain.meeting.enums.MeetingStatus;
import com.example.momo.global.common.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
	private Integer categoryId;

	@Column(nullable = false, name = "current_participants_count")
	private int currentParticipantsCount;

	@Column(nullable = false, name = "max_participants_count")
	private Integer maxParticipantsCount;

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

	@Builder.Default
	@Column(nullable = false, name = "min_enter_score")
	private Double minEnterScore = 0.0;

	@Builder.Default
	@OneToMany(cascade = {CascadeType.PERSIST})
	@JoinColumn(name = "meeting_id")
	private List<MeetingParticipant> participants = new ArrayList<>();

	@Column(nullable = false, name = "participation_fee")
	private int participationFee;

	// TODO : 추후 Converter 변경 고려
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "status")
	private MeetingStatus status; // 모집중, 모집완료

	@Version
	private Long version;

	public void updateMeeting(MeetingUpdateRequestDto request) {
		this.title = request.getTitle();
		this.description = request.getDescription();
		this.categoryId = request.getCategoryId();
		this.maxParticipantsCount = request.getMaxParticipantsCount();
		this.meetingDate = request.getMeetingDate();
		this.meetingEndDate = request.getMeetingEndDate();
		this.locationName = request.getLocationName();
		this.latitude = request.getLatitude();
		this.longitude = request.getLongitude();
		this.minEnterScore = request.getMinEnterScore();
		this.participationFee = request.getParticipationFee();
		this.status = request.getStatus();
	}

	public void updateStatus(MeetingStatus status) {
		this.status = status;
	}

	public void addMeetingParticipant() {
		this.currentParticipantsCount++;
	}

	public void removeMeetingParticipant() {
		this.currentParticipantsCount--;
	}
}
