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

@Table(name = "user_follow")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFollow extends BaseCreateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, name = "follower_id")
	private Long followerId;

	@Column(nullable = false, name = "following_id")
	private Long followingId;

	UserFollow(Long followerId, Long followingId) {
		this.followerId = followerId;
		this.followingId = followingId;
	}
}
