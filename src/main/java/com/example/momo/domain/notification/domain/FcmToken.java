package com.example.momo.domain.notification.domain;

import com.example.momo.domain.notification.enums.PlatformType;
import com.example.momo.global.common.entity.BaseCreateEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fcm_token", uniqueConstraints = {
	@UniqueConstraint(name = "uq_user_token", columnNames = {"user_id", "token"})
})
public class FcmToken extends BaseCreateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(nullable = false)
	private String token;

	@Enumerated(EnumType.STRING)
	@Column(name = "platform_type", nullable = false)
	private PlatformType platformType;
}
