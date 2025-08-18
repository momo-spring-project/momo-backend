package com.example.momo.domain.meeting.domain;

import com.example.momo.domain.meeting.application.dto.response.ParticipantResponseDto;
import com.example.momo.global.common.entity.BaseCreateEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "meeting_participants")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingParticipant extends BaseCreateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, name = "meeting_id")
	private Meeting meeting;

	@Column(nullable = false, name = "user_id")
	private Long userId;

	@Column(name = "attendance_status")
	private Boolean attendanceStatus = false;

	private MeetingParticipant(Meeting meeting, Long userId) {
		this.meeting = meeting;
		this.userId = userId;
	}

	public static MeetingParticipant createParticipant(Meeting meeting, Long userId) {
		return new MeetingParticipant(meeting, userId);
	}

	public void updateAttendanceStatus() {
		this.attendanceStatus = true;
	}
}
