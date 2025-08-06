package com.example.momo.domain.user.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.momo.domain.user.domain.UserOutboxEvent;

public interface UserOutboxJpaRepository extends JpaRepository<UserOutboxEvent, Long> {
}
