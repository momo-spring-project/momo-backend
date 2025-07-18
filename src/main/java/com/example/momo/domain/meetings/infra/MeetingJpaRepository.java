package com.example.momo.domain.meetings.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.meetings.domain.Meeting;

@Repository
public interface MeetingJpaRepository extends JpaRepository<Meeting, Long> {
}
