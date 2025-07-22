package com.example.momo.domain.category.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "categories")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, unique = true, name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	public Category(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public void updateName(String categoryName){
		this.name = categoryName;
	}

	public void updateDescription(String categoryDescription){
		this.description = categoryDescription;
	}
}