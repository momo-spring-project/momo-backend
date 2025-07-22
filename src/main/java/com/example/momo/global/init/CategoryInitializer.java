package com.example.momo.global.init;

import com.example.momo.domain.category.domain.Category;
import com.example.momo.domain.category.infra.CategoryRepository;
import com.example.momo.domain.category.application.CategoryService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

// 카테고리 초기 생성
// 개발 단계에서만 사용
// @Component
@RequiredArgsConstructor
public class CategoryInitializer {

	private final CategoryService categoryService;
	private final CategoryRepository categoryRepository;

	@PostConstruct
	public void init() {
		categoryRepository.save(new Category("music_concert", "음악 / 콘서트"));
		categoryRepository.save(new Category("movie_drama", "영화 / 드라마"));
		categoryRepository.save(new Category("reading", "독서"));
		categoryRepository.save(new Category("yoga_pilates", "요가 / 필라테스"));
		categoryRepository.save(new Category("running_marathon", "러닝 / 마라톤"));
		categoryRepository.save(new Category("hiking", "등산"));
		categoryRepository.save(new Category("fitness_crossfit", "헬스 / 크로스핏"));
		categoryRepository.save(new Category("cycling", "자전거"));
		categoryRepository.save(new Category("camping_car_camping", "캠핑 / 차박"));
		categoryRepository.save(new Category("food_tour", "맛집 탐방"));
		categoryRepository.save(new Category("overseas_travel", "해외 여행"));
		categoryRepository.save(new Category("finance_investment", "제태크 / 투자"));
		categoryRepository.save(new Category("coding_development", "코딩 / 개발"));
		categoryRepository.save(new Category("language_study", "외국어 공부"));
		categoryRepository.save(new Category("design_uiux", "디자인 / UIUX"));
		categoryRepository.save(new Category("peer_friend_finder", "또래 친구 찾기"));
		categoryRepository.save(new Category("pet_gathering", "반려동물 모임"));
		categoryRepository.save(new Category("local_community", "지역 기반 모임"));
		categoryRepository.save(new Category("after_work_beer", "퇴근 후 맥주 한잔"));
		categoryRepository.save(new Category("job_community", "직무 커뮤니티"));
	}
}