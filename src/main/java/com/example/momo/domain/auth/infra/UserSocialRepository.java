package com.example.momo.domain.auth.infra;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.momo.domain.auth.domain.UserSocial;

public interface UserSocialRepository extends JpaRepository<UserSocial, Long> {
	@EntityGraph(attributePaths = "user")
	UserSocial findByProviderId(String providerId);
}
