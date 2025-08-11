package com.example.momo.domain.user.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.momo.global.common.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
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

	@Column(name = "password")
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

	// === 연관관계 (OneToMany 단방향) ===
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private List<UserCategory> categories = new ArrayList<>();

	// 내가 팔로잉을 하는 사람들의 리스트
	@OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private List<UserFollow> followings = new ArrayList<>();

	// 내가 받은 평가들
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
	@JoinColumn(name = "target_user_id")
	private List<UserRating> ratings = new ArrayList<>();

	// === 정적 팩토리 메서드 ===

	/**
	 * 일반 회원가입용 사용자 생성
	 */
	public static User createUser(String nickname, String email, String encodedPassword,
		Double latitude, Double longitude) {
		User user = new User();
		user.nickname = nickname;
		user.email = email;
		user.password = encodedPassword;
		user.score = 50.0; // 기본 점수
		user.followingCount = 0;
		user.followerCount = 0;
		user.latitude = latitude;
		user.longitude = longitude;
		user.categories = new ArrayList<>();
		user.followings = new ArrayList<>();
		user.ratings = new ArrayList<>();

		return user;
	}

	// === 하위 엔티티 관리 메서드 (User 애그리거트 책임) ===

	public void addRating(Long reviewerId, Long meetingId, Integer ratingScore) {
		UserRating rating = new UserRating(reviewerId, this.id, meetingId, ratingScore);
		this.ratings.add(rating);
	}

	public void addFollowing(User following) {
		UserFollow userFollow = new UserFollow(this, following);
		this.followings.add(userFollow);
		this.incrementFollowingCount();
	}

	public void removeFollowing(User following) {
		this.followings.removeIf(follow ->
			follow.getFollowing().getId().equals(following.getId()));
		this.decrementFollowingCount();
	}

	public void increaseFollowerCount() {
		this.followerCount++;
	}

	public void decreaseFollowerCount() {
		this.followerCount--;
	}

	public void incrementFollowingCount() {
		this.followingCount++;
	}

	public void decrementFollowingCount() {
		this.followingCount--;
	}

	public void updateCategories(List<Integer> categoryIds) {
		this.categories.clear();
		if (categoryIds != null && !categoryIds.isEmpty()) {
			for (Integer categoryId : categoryIds) {
				UserCategory category = new UserCategory(this, categoryId);
				this.categories.add(category);
			}
		}
	}

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

	public void updateLocation(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
}
