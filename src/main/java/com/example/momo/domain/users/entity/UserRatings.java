package com.example.momo.domain.users.entity;

import com.example.momo.domain.common.entity.BaseCreateEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "user_ratings")
@Getter
@Entity
@NoArgsConstructor
public class UserRatings extends BaseCreateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, name = "target_user_id")
	private Users targetUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, name = "reviewer_id")
	private Users reviewer;

	@Column(nullable = false)
	private Long meetingId;

	@Column(nullable = false, name = "rating_score")
	private Double ratingScore;

	public UserRatings(Users targetUser, Users reviewer, Long meetingId, Double ratingScore) {
		this.targetUser = targetUser;
		this.reviewer = reviewer;
		this.meetingId = meetingId;
		this.ratingScore = ratingScore;
	}
}
