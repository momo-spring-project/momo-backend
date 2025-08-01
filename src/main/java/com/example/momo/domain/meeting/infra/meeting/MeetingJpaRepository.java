package com.example.momo.domain.meeting.infra.meeting;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.meeting.domain.Meeting;

@Repository
public interface MeetingJpaRepository extends JpaRepository<Meeting, Long> {

	Optional<Meeting> findByIdAndIsDeletedFalse(Long id);
}

