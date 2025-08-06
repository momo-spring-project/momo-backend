package com.example.momo.domain.user.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.momo.domain.user.domain.User;

public interface UserJpaRepository extends JpaRepository<User, Long> {

	// ==================== 사용자 중복 및 존재 여부 확인 ====================

	// 닉네임 중복 여부 확인 (자기 자신 제외)
	boolean existsByNicknameAndIdNot(String nickname, Long id);

	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	// 존재하는 사용자 ID만 조회 (삭제되지 않은 사용자만)
	@Query("SELECT u.id FROM User u WHERE u.id IN :userIds AND u.isDeleted = false")
	List<Long> findExistingUserIds(@Param("userIds") List<Long> userIds);

	// ==================== 사용자 조회 ====================

	@Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.categories WHERE u.id IN :userIds AND u.isDeleted = false")
	List<User> findAllByIdInAndIsDeletedFalse(List<Long> userIds);

	Optional<User> findByEmailAndIsDeletedFalse(String email);

	Optional<User> findByIdAndIsDeletedFalse(Long id);

	// ==================== 팔로우 시스템 ====================

	@Query("""
			SELECT u FROM User u
			WHERE u.id IN (
				SELECT uf.followingId
				FROM UserFollow uf
				WHERE uf.followerId = :userId
			)
			AND u.isDeleted = false
			ORDER BY u.id
		""")
	Slice<User> findFollowingsByUserId(@Param("userId") Long userId, Pageable pageable);

	@Query("""
			SELECT u FROM User u
			WHERE u.id IN (
				SELECT uf.followerId
				FROM UserFollow uf
				WHERE uf.followingId = :userId
			)
			AND u.isDeleted = false
			ORDER BY u.id
		""")
	Slice<User> findFollowersByUserId(@Param("userId") Long userId, Pageable pageable);

	@Modifying
	@Query("DELETE FROM UserFollow uf WHERE uf.followerId = :followerId AND uf.followingId = :followingId")
	int deleteUserFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
}