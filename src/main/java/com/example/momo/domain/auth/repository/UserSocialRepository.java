package com.example.momo.domain.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.momo.domain.auth.entity.UserSocial;
import com.example.momo.domain.user.domain.User;

public interface UserSocialRepository extends JpaRepository<UserSocial, Long> {
	@EntityGraph(attributePaths = "user")
	UserSocial findByProviderId(String providerId);

	List<UserSocial> findAllByUser(User user);
}
