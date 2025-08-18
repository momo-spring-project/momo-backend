package com.example.momo.domain.user.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface UserRepository {

	// ==================== 사용자 중복 및 존재 여부 확인 ====================

	boolean isDuplicateNickname(String nickname, Long id);

	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	// ==================== 사용자 조회 ====================

	// 다중 사용자 조회 (삭제되지 않은 사용자만)
	List<User> findAllByIdInAndIsDeletedFalse(List<Long> userIds);

	List<Long> findExistingUserIds(List<Long> userIds);

	/**
	 * 카테고리, 위도, 경도 기반으로 사용자를 필터링하여 조회
	 *
	 * @param categoryIds 관심 카테고리 ID 목록 (null이면 필터링 안함)
	 * @param latitude 위도 (null이면 필터링 안함)
	 * @param longitude 경도 (null이면 필터링 안함)
	 * @return 필터링된 사용자 목록
	 */
	List<User> getUsersByLocationAndCategory(
		List<Integer> categoryIds,
		Double latitude,
		Double longitude
	);

	// ==================== 팔로우 시스템 ====================

	Slice<User> findFollowingsByUserId(Long userId, Pageable pageable);

	Slice<User> findFollowersByUserId(Long userId, Pageable pageable);

	// ==================== 사용자 저장 및 단일 조회 ====================

	void save(User user);

	Optional<User> findByEmailAndIsDeletedFalse(String email);

	Optional<User> findByIdAndIsDeletedFalse(Long id);
}