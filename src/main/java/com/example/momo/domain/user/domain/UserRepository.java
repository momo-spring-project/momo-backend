package com.example.momo.domain.user.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface UserRepository {

	boolean existsByNicknameAndIdNot(String nickname, Long id);

	/**
	 * 다중 사용자 조회 (삭제되지 않은 사용자만)
	 *
	 * @param userIds 조회할 사용자 ID 목록
	 * @return 사용자 엔티티 목록
	 */
	List<User> findAllByIdInAndIsDeletedFalse(List<Long> userIds);

	/**
	 * 존재하는 사용자 ID만 조회
	 *
	 * @param userIds 확인할 사용자 ID 목록
	 * @return 존재하는 사용자 ID 목록
	 */
	List<Long> findExistingUserIds(List<Long> userIds);

	/**
	 * 특정 사용자가 팔로잉하는 사용자들 조회 (COUNT 쿼리 없음)
	 * @param userId 조회할 사용자 ID
	 * @param pageable 페이징 정보
	 * @return 팔로잉하는 사용자 목록 (Slice)
	 */
	Slice<User> findFollowingsByUserId(Long userId, Pageable pageable);

	/**
	 * 특정 사용자를 팔로우하는 사용자들 조회 (COUNT 쿼리 없음)
	 * @param userId 조회할 사용자 ID
	 * @param pageable 페이징 정보
	 * @return 팔로워 목록 (Slice)
	 */
	Slice<User> findFollowersByUserId(Long userId, Pageable pageable);

	/**
	 * 팔로우 관계 삭제 (물리 삭제)
	 * @param followerId 팔로워 ID
	 * @param followingId 팔로잉 대상 ID
	 * @return 삭제된 행 개수
	 */
	int deleteUserFollow(Long followerId, Long followingId);

	// Auth 쪽에서 사용
	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	void save(User user);

	Optional<User> findByEmailAndIsDeletedFalse(String email);

	Optional<User> findByIdAndIsDeletedFalse(Long id);
}