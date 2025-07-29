package com.example.momo.global.init;

import org.springframework.stereotype.Component;

import com.example.momo.domain.category.application.CategoryService;
import com.example.momo.domain.category.domain.Category;
import com.example.momo.domain.category.infra.CategoryRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryInitializer {

	private final CategoryService categoryService;
	private final CategoryRepository categoryRepository;

	@PostConstruct
	public void init() {
		// 이미 카테고리가 존재하면 초기화 건너뛰기
		if (categoryRepository.count() > 0) {
			log.info("카테고리가 이미 존재합니다. 초기화를 건너뜁니다.");
			return;
		}

		log.info("카테고리 초기 데이터를 생성합니다.");

		try {
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

			log.info("카테고리 초기 데이터 생성 완료");
		} catch (Exception e) {
			log.error("카테고리 초기 데이터 생성 중 오류 발생", e);
			// 오류가 발생해도 애플리케이션 시작을 막지 않음
		}
	}
}