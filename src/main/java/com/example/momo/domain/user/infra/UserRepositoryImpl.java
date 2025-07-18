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
}