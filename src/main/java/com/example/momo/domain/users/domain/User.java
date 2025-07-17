package com.example.momo.domain.users.domain;

import com.example.momo.domain.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

	@Column(nullable = false, unique = true, name = "email")
	private String email;

	@Column(nullable = false, name = "password")
	private String password;

	@Column(name = "score")
	private Double score = 50.0;

	@Column(name = "latitude")
	private Double latitude;

	@Column(name = "longitude")
	private Double longitude;

	@Builder
	public User(String nickname, String email, String password,
				Integer score, Double latitude, Double longitude) {
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

	// 팔로우
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn(name = "follower_id")
	private List<UserFollow> following = new ArrayList<>();

	// 내가 받은 평가들
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn(name = "target_user_id")
	private List<UserRating> ratings = new ArrayList<>();
}
