package com.example.momo.domain.user.infra;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.momo.domain.user.domain.QUser;
import com.example.momo.domain.user.domain.QUserCategory;
import com.example.momo.domain.user.domain.User;
import com.example.momo.global.utils.LocationUtils;
import com.example.momo.global.utils.dto.LatLngBounds;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserQueryRepository {

	private final JPAQueryFactory queryFactory;

	/**
	 * 카테고리, 위도, 경도 기반으로 사용자를 필터링하여 조회
	 *
	 * @param categoryIds 관심 카테고리 ID 목록 (null이면 필터링 안함)
	 * @param latitude 위도 (null이면 필터링 안함)
	 * @param longitude 경도 (null이면 필터링 안함)
	 * @return 필터링된 사용자 목록
	 */
	public List<User> getUsersByLocationAndCategory(
		List<Integer> categoryIds,
		Double latitude,
		Double longitude
	) {
		QUser user = QUser.user;
		QUserCategory userCategory = QUserCategory.userCategory;

		BooleanBuilder builder = new BooleanBuilder();

		// 삭제되지 않은 사용자만 조회
		builder.and(user.isDeleted.eq(false));

		// 카테고리 필터링
		if (categoryIds != null && !categoryIds.isEmpty()) {
			builder.and(
				user.categories.any().categoryId.in(categoryIds)
			);
		}

		if (latitude != null && longitude != null) {
			LatLngBounds bounds = LocationUtils.calculate10KmBounds(latitude, longitude);

			builder.and(
				user.latitude.between(bounds.minLatitude(), bounds.maxLatitude())
					.and(user.longitude.between(bounds.minLongitude(), bounds.maxLongitude()))
			);
		}

		// 결과 조회 (점수 높은 순, ID 오름차순)
		return queryFactory
			.selectFrom(user)
			.leftJoin(user.categories, userCategory).fetchJoin()
			.where(builder)
			.orderBy(user.score.desc(), user.id.asc())
			.fetch();
	}
}