package com.example.momo.domain.user.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.user.domain.User;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {

	boolean existsByNicknameAndIdNot(String nickname, Long id);

	/**
	 * 다중 사용자 조회 (삭제되지 않은 사용자만)
	 */
	List<User> findAllByIdInAndIsDeletedFalse(List<Long> userIds);

	/**
	 * 존재하는 사용자 ID만 조회 (삭제되지 않은 사용자만)
	 */
	@Query("SELECT u.id FROM User u WHERE u.id IN :userIds AND u.isDeleted = false")
	List<Long> findExistingUserIds(@Param("userIds") List<Long> userIds);
	/**
	 * 특정 사용자가 팔로잉하는 사용자들 조회 (Slice 사용으로 COUNT 쿼리 방지)
	 */
	@Query("SELECT u FROM User u " +
		"WHERE u.id IN (SELECT uf.followingId FROM UserFollow uf WHERE uf.followerId = :userId) " +
		"AND u.isDeleted = false " +
		"ORDER BY u.id")
	Slice<User> findFollowingsByUserId(@Param("userId") Long userId, Pageable pageable);

	/**
	 * 특정 사용자를 팔로우하는 사용자들 조회 (Slice 사용으로 COUNT 쿼리 방지)
	 */
	@Query("SELECT u FROM User u " +
		"WHERE u.id IN (SELECT uf.followerId FROM UserFollow uf WHERE uf.followingId = :userId) " +
		"AND u.isDeleted = false " +
		"ORDER BY u.id")
	Slice<User> findFollowersByUserId(@Param("userId") Long userId, Pageable pageable);

	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	Optional<User> findByEmailAndIsDeletedFalse(String email);

	Optional<User> findByIdAndIsDeletedFalse(Long id);

	@Modifying
	@Query("DELETE FROM UserFollow uf WHERE uf.followerId = :followerId AND uf.followingId = :followingId")
	int deleteUserFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
}