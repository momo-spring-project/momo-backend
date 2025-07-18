package com.example.momo.domain.user.infra;

import java.util.Optional;

import com.example.momo.domain.user.domain.User;

public interface UserRepository {
	Optional<User> findById(Long id);

	boolean existsByEmailAndIdNot(String email, Long id);

	boolean existsByNicknameAndIdNot(String nickname, Long id);
}