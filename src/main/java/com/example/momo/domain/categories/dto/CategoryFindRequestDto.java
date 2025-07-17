package com.example.momo.domain.categories.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CategoryFindRequestDto {
	private List<Integer> categoryIds;
}