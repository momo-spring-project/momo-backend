package com.example.momo.domain.users.domain;

import com.example.momo.domain.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

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


	// === 연관관계 (OneToMany 단방향) ===

	// 사용자 관심 카테고리
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "user_id")
	private List<UserCategory> categories = new ArrayList<>();

	// 팔로우
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "follower_id")
	private List<UserFollow> following = new ArrayList<>();

	// 내가 받은 평가들
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "target_user_id")
	private List<UserRating> ratings = new ArrayList<>();
}
