package com.example.momo.domain.meetings.infra;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.meetings.domain.Meeting;

@Repository
public interface MeetingJpaRepository extends JpaRepository<Meeting, Long> {
	Optional<Meeting> findByIdAndIsDeletedFalse(Long id);

	Page<Meeting> findAllByTitleContaining(String title, Pageable pageable);
}

