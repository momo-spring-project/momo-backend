package com.example.momo.domain.meeting.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.momo.domain.meeting.application.dto.request.MeetingUpdateRequestDto;
import com.example.momo.domain.meeting.enums.MeetingStatus;
import com.example.momo.global.common.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "meetings", indexes = {
	@Index(name = "idx_meeting_deleted_category_status_date", columnList = "is_deleted, category_id, status, meeting_date")
})
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meeting extends BaseEntity {

	@OneToMany(mappedBy = "meetingId", cascade = {CascadeType.PERSIST,
		CascadeType.REMOVE}, fetch = FetchType.LAZY, orphanRemoval = true)
	private final List<MeetingParticipant> participants = new ArrayList<>();
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
	@Column(nullable = false, name = "min_enter_score")
	private Double minEnterScore = 0.0;
	@Column(nullable = false, name = "participation_fee")
	private int participationFee;

	// TODO : 추후 Converter 변경 고려
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "status")
	private MeetingStatus status; // 모집중, 모집완료

	@Version
	private Integer version;

	private Meeting(Long hostUserId, String title, String description, Integer categoryId,
		int currentParticipantsCount, Integer maxParticipantsCount, LocalDateTime meetingDate,
		LocalDateTime meetingEndDate,
		String locationName, Double latitude, Double longitude, Double minEnterScore,
		int participationFee, MeetingStatus status) {

		this.hostUserId = hostUserId;
		this.title = title;
		this.description = description;
		this.categoryId = categoryId;
		this.currentParticipantsCount = currentParticipantsCount;
		this.maxParticipantsCount = maxParticipantsCount;
		this.meetingDate = meetingDate;
		this.meetingEndDate = meetingEndDate;
		this.locationName = locationName;
		this.latitude = latitude;
		this.longitude = longitude;
		this.minEnterScore = minEnterScore;
		this.participationFee = participationFee;
		this.status = status;
	}

	public static Meeting create(Long hostUserId, String title, String description, Integer categoryId,
		int currentParticipantsCount, Integer maxParticipantsCount, LocalDateTime meetingDate,
		LocalDateTime meetingEndDate,
		String locationName, Double latitude, Double longitude, Double minEnterScore,
		int participationFee, MeetingStatus status) {

		return new Meeting(hostUserId, title, description, categoryId,
			currentParticipantsCount, maxParticipantsCount, meetingDate, meetingEndDate,
			locationName, latitude, longitude, minEnterScore, participationFee, status);
	}

	public void updateStatus(MeetingStatus status) {
		this.status = status;
	}

	public void updateMeeting(MeetingUpdateRequestDto request) {
		this.title = request.title();
		this.description = request.description();
		this.categoryId = request.categoryId();
		this.maxParticipantsCount = request.maxParticipantsCount();
		this.meetingDate = request.meetingDate();
		this.meetingEndDate = request.meetingEndDate();
		this.locationName = request.locationName();
		this.latitude = request.latitude();
		this.longitude = request.longitude();
		this.minEnterScore = request.minEnterScore();
		this.participationFee = request.participationFee();
		this.status = request.status();
	}

	public void addMeetingParticipant() {
		this.currentParticipantsCount++;
	}

	public void removeMeetingParticipant() {
		this.currentParticipantsCount--;
	}

	public void removeMeeting() {
		this.getParticipants().clear();
		this.currentParticipantsCount = 0;
	}
}
