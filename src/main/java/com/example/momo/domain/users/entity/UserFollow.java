package com.example.momo.domain.users.entity;

import com.example.momo.domain.common.entity.BaseCreateEntity;

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

@Table(name = "user_follow")
@Getter
@Entity
@NoArgsConstructor
public class UserFollow extends BaseCreateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, name = "follower_id")
	private Users follower;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, name = "following_id")
	private Users following;

	public UserFollow(Users follower, Users following) {
		this.follower = follower;
		this.following = following;
	}
}
