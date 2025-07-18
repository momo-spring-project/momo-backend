package com.example.momo.domain.user.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.example.momo.domain.user.domain.User;

public interface UserRepository {
	Optional<User> findById(Long id);

	boolean existsByEmailAndIdNot(String email, Long id);

	boolean existsByNicknameAndIdNot(String nickname, Long id);

	/**
	 * 특정 사용자가 팔로잉하는 사용자들 조회 (페이징)
	 * @param userId 조회할 사용자 ID
	 * @param pageable 페이징 정보
	 * @return 팔로잉하는 사용자 목록
	 */
	List<User> findFollowingsByUserId(Long userId, Pageable pageable);

	/**
	 * 특정 사용자를 팔로우하는 사용자들 조회 (페이징)
	 * @param userId 조회할 사용자 ID
	 * @param pageable 페이징 정보
	 * @return 팔로워 목록
	 */
	List<User> findFollowersByUserId(Long userId, Pageable pageable);
}
}