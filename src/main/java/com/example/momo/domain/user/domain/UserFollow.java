package com.example.momo.domain.user.domain;

import com.example.momo.global.common.entity.BaseCreateEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "user_follow")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFollow extends BaseCreateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "follower_id", nullable = false)
	private User follower;  // 팔로우 하는 사람

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "following_id", nullable = false)
	private User following; // 팔로우 받는 사람

	UserFollow(User follower, User following) {
		this.follower = follower;
		this.following = following;
	}
}
