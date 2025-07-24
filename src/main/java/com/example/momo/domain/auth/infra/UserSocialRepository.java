package com.example.momo.domain.auth.infra;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.momo.domain.auth.domain.UserSocial;
import com.example.momo.domain.user.domain.User;

public interface UserSocialRepository extends JpaRepository<UserSocial, Long> {
	UserSocial findByProviderId(String providerId);

	List<UserSocial> findAllByUserId(Long userId);
}
