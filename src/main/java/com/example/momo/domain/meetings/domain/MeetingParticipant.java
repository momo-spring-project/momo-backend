package com.example.momo.domain.meetings.domain;

import com.example.momo.domain.common.entity.BaseCreateEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

	@Column(nullable = false, name = "meeting_id")
	private Long meeting;

	@Column(nullable = false, name = "user_id")
	private Long userId;

	@Column(name = "attendance_status")
	private Boolean attendanceStatus;
}
