package com.example.momo.domain.user.infra;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.momo.domain.user.domain.User;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

	private final UserJpaRepository userJpaRepository;

	@Override
	public Optional<User> findById(Long id) {
		return userJpaRepository.findById(id);
	}

	@Override
	public boolean existsByEmailAndIdNot(String email, Long id) {
		return userJpaRepository.existsByEmailAndIdNot(email, id);
	}

	@Override
	public boolean existsByNicknameAndIdNot(String nickname, Long id) {
		return userJpaRepository.existsByNicknameAndIdNot(nickname, id);
	}

	@Override
	public List<User> findFollowingsByUserId(Long userId, Pageable pageable) {
		return userJpaRepository.findFollowingsByUserId(userId, pageable);
	}

	@Override
	public List<User> findFollowersByUserId(Long userId, Pageable pageable) {
		return userJpaRepository.findFollowersByUserId(userId, pageable);
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
	public void save(User user) {
		userJpaRepository.save(user);
	}

	@Override
	public Optional<User> findByEmailAndIsDeletedFalse(String email) {
		return userJpaRepository.findByEmailAndIsDeletedFalse(email);
	}

	@Override
	public Optional<User> findByIdAndIsDeletedFalse(Long id) {
		return userJpaRepository.findByIdAndIsDeletedFalse(id);
	}
}