package com.example.momo.domain.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "user_categories")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "category_id", nullable = false)
	private Integer categoryId;

	public UserCategory(Integer categoryId) {
		this.categoryId = categoryId;
	}
}