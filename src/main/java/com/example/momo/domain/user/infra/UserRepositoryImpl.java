package com.example.momo.domain.user.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

	private final UserJpaRepository userJpaRepository;
	private final UserQueryRepository userQueryRepository;

	// ==================== 사용자 중복 및 존재 확인 ====================

	@Override
	public boolean isDuplicateNickname(String nickname, Long id) {
		return userJpaRepository.existsByNicknameAndIdNot(nickname, id);
	}

	@Override
	public boolean existsByEmail(String email) {
		return userJpaRepository.existsByEmail(email);
	}

	@Override
	public boolean existsByNickname(String nickname) {
		return userJpaRepository.existsByNickname(nickname);
	}

	@Override
	public List<Long> findExistingUserIds(List<Long> userIds) {
		return userJpaRepository.findExistingUserIds(userIds);
	}

	// ==================== 사용자 조회 ====================

	@Override
	public List<User> findAllByIdInAndIsDeletedFalse(List<Long> userIds) {
		return userJpaRepository.findAllByIdInAndIsDeletedFalse(userIds);
	}

	@Override
	public Optional<User> findByEmailAndIsDeletedFalse(String email) {
		return userJpaRepository.findByEmailAndIsDeletedFalse(email);
	}

	@Override
	public Optional<User> findByIdAndIsDeletedFalse(Long id) {
		return userJpaRepository.findByIdAndIsDeletedFalse(id);
	}

	@Override
	public List<User> getUsersByLocationAndCategory(
		List<Integer> categoryIds,
		Double latitude,
		Double longitude
	) {
		return userQueryRepository.getUsersByLocationAndCategory(categoryIds, latitude, longitude);
	}

	// ==================== 팔로우 기능 ====================

	@Override
	public Slice<User> findFollowingsByUserId(Long userId, Pageable pageable) {
		return userJpaRepository.findFollowingsByUserId(userId, pageable);
	}

	@Override
	public Slice<User> findFollowersByUserId(Long userId, Pageable pageable) {
		return userJpaRepository.findFollowersByUserId(userId, pageable);
	}

	@Override
	public int deleteUserFollow(Long followerId, Long followingId) {
		return userJpaRepository.deleteUserFollow(followerId, followingId);
	}

	// ==================== 저장 ====================

	@Override
	public void save(User user) {
		userJpaRepository.save(user);
	}
}