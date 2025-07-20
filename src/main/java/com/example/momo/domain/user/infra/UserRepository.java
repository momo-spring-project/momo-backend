package com.example.momo.domain.user.infra;

import java.util.Optional;

import com.example.momo.domain.user.domain.User;

public interface UserRepository {
	Optional<User> findById(Long id);

	boolean existsByEmailAndIdNot(String email, Long id);

	boolean existsByNicknameAndIdNot(String nickname, Long id);
	// ----------------------------------
	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	void save(User user);

	Optional<User> findByEmailAndIsDeletedFalse(String email);

	Optional<User> findByIdAndIsDeletedFalse(Long id);
}