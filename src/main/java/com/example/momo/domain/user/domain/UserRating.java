package com.example.momo.domain.user.domain;

import com.example.momo.global.common.entity.BaseCreateEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "user_ratings")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRating extends BaseCreateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, name = "target_user_id")
	private Long targetUserId;

	@Column(nullable = false, name = "reviewer_id")
	private Long reviewerId;

	@Column(nullable = false, name = "meeting_id")
	private Long meetingId;

	@Column(nullable = false, name = "rating_score")
	private Integer ratingScore;

	UserRating(Long reviewerId, Long targetUserId, Long meetingId, Integer ratingScore) {
		this.reviewerId = reviewerId;
		this.targetUserId = targetUserId;
		this.meetingId = meetingId;
		this.ratingScore = ratingScore;
	}
}
