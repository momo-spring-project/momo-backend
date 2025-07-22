package com.example.momo.domain.user.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.momo.domain.common.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "users")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, name = "nickname")
	private String nickname;

	@Column(nullable = false, name = "email")
	private String email;

	@Column(nullable = false, name = "password")
	private String password;

	@Column(name = "score")
	private Double score = 50.0;

	@Column(name = "following_count", nullable = false)
	private Integer followingCount = 0;

	@Column(name = "follower_count", nullable = false)
	private Integer followerCount = 0;

	@Column(name = "latitude")
	private Double latitude;

	@Column(name = "longitude")
	private Double longitude;

	@Builder
	public User(String nickname, String email, String password,
		Double score, Double latitude, Double longitude) {
		this.nickname = nickname;
		this.email = email;
		this.password = password;
		this.score = score != null ? score : 50.0;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	// === 연관관계 (OneToMany 단방향) ===
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn(name = "user_id")
	private List<UserCategory> categories = new ArrayList<>();

	// 내가 팔로잉을 하는 사람들의 리스트
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn(name = "follower_id")
	private List<UserFollow> followings = new ArrayList<>();

	// 내가 받은 평가들
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn(name = "target_user_id")
	private List<UserRating> ratings = new ArrayList<>();

	// === 업데이트 메서드들 ===
	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	public void updatePassword(String password) {
		this.password = password;
	}

	public void updateScore(Double score) {
		this.score = score;
	}

	public void incrementFollowingCount() {
		this.followingCount++;
	}

	public void decrementFollowingCount() {
		this.followingCount--;
	}

	public void incrementFollowerCount() {
		this.followerCount++;
	}

	public void decrementFollowerCount() {
		this.followerCount--;
	}

	public void updateLocation(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public void updateCategories(List<Integer> categoryIds) {
		this.categories.clear();
		categoryIds.forEach(categoryId ->
			this.categories.add(new UserCategory(categoryId))
		);
	}
}
