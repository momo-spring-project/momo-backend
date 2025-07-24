package com.example.momo.global.infrastructure.client.category.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryClientResponseDto {
	private Integer id;
	private String name;
	private String description;
}
