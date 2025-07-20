package com.example.momo.domain.user.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.user.domain.User;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {

	boolean existsByEmailAndIdNot(String email, Long id);

	boolean existsByNicknameAndIdNot(String nickname, Long id);

	/**
	 * 특정 사용자가 팔로잉하는 사용자들 조회
	 * UserFollow 테이블을 통해 following_id에 해당하는 User들을 조회
	 */
	@Query("SELECT u FROM User u " +
		"WHERE u.id IN (SELECT uf.followingId FROM UserFollow uf WHERE uf.followerId = :userId)")
	List<User> findFollowingsByUserId(@Param("userId") Long userId, Pageable pageable);

	/**
	 * 특정 사용자를 팔로우하는 사용자들 조회
	 * UserFollow 테이블을 통해 follower_id에 해당하는 User들을 조회
	 */
	@Query("SELECT u FROM User u " +
		"WHERE u.id IN (SELECT uf.followerId FROM UserFollow uf WHERE uf.followingId = :userId)")
	List<User> findFollowersByUserId(@Param("userId") Long userId, Pageable pageable);

	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	Optional<User> findByEmailAndIsDeletedFalse(String email);

	Optional<User> findByIdAndIsDeletedFalse(Long id);
}