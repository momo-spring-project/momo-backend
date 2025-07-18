package com.example.momo.domain.user.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.user.domain.User;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {

	boolean existsByEmailAndIdNot(String email, Long id);

	boolean existsByNicknameAndIdNot(String nickname, Long id);
}