package com.example.momo.domain.auth.domain;

import com.example.momo.domain.auth.enums.OAuth2Type;
import com.example.momo.domain.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Table(name = "user_social")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UserSocial {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;

	private String providerId;

	@Enumerated(EnumType.STRING)
	private OAuth2Type type;

	protected UserSocial(Long userId, String providerId, OAuth2Type type) {
		this.userId = userId;
		this.providerId = providerId;
		this.type = type;
	}

	public static UserSocial of(Long userId, String providerId, OAuth2Type type) {
		return new UserSocial(userId, providerId, type);
	}

}
