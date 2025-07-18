package com.example.momo.global.config;

import com.example.momo.domain.categories.dto.CategoryAddRequestDto;
import com.example.momo.domain.categories.service.CategoryService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 카테고리 초기 생성
// 개발 단계에서만 사용
@Component
@RequiredArgsConstructor
public class CategoryInitializer {

	private final CategoryService categoryService;

	@PostConstruct
	public void init() {
		categoryService.addCategory(new CategoryAddRequestDto("music_concert", "음악 / 콘서트"));
		categoryService.addCategory(new CategoryAddRequestDto("movie_drama", "영화 / 드라마"));
		categoryService.addCategory(new CategoryAddRequestDto("reading", "독서"));
		categoryService.addCategory(new CategoryAddRequestDto("yoga_pilates", "요가 / 필라테스"));
		categoryService.addCategory(new CategoryAddRequestDto("running_marathon", "러닝 / 마라톤"));
		categoryService.addCategory(new CategoryAddRequestDto("hiking", "등산"));
		categoryService.addCategory(new CategoryAddRequestDto("fitness_crossfit", "헬스 / 크로스핏"));
		categoryService.addCategory(new CategoryAddRequestDto("cycling", "자전거"));
		categoryService.addCategory(new CategoryAddRequestDto("camping_car_camping", "캠핑 / 차박"));
		categoryService.addCategory(new CategoryAddRequestDto("food_tour", "맛집 탐방"));
		categoryService.addCategory(new CategoryAddRequestDto("overseas_travel", "해외 여행"));
		categoryService.addCategory(new CategoryAddRequestDto("finance_investment", "제태크 / 투자"));
		categoryService.addCategory(new CategoryAddRequestDto("coding_development", "코딩 / 개발"));
		categoryService.addCategory(new CategoryAddRequestDto("language_study", "외국어 공부"));
		categoryService.addCategory(new CategoryAddRequestDto("design_uiux", "디자인 / UIUX"));
		categoryService.addCategory(new CategoryAddRequestDto("peer_friend_finder", "또래 친구 찾기"));
		categoryService.addCategory(new CategoryAddRequestDto("pet_gathering", "반려동물 모임"));
		categoryService.addCategory(new CategoryAddRequestDto("local_community", "지역 기반 모임"));
		categoryService.addCategory(new CategoryAddRequestDto("after_work_beer", "퇴근 후 맥주 한잔"));
		categoryService.addCategory(new CategoryAddRequestDto("job_community", "직무 커뮤니티"));
	}
}