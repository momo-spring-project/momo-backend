package com.example.momo.domain.users.domain;

import com.example.momo.domain.common.entity.BaseCreateEntity;

import jakarta.persistence.*;
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

	public UserFollow(Long followerId, Long followingId) {
		this.followerId = followerId;
		this.followingId = followingId;
	}
}
