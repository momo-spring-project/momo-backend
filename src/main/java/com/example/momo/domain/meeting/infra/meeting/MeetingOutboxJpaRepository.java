package com.example.momo.domain.meeting.infra.meeting;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.meeting.domain.MeetingElasticsearchOutbox;

@Repository
public interface MeetingOutboxJpaRepository extends JpaRepository<MeetingElasticsearchOutbox, Long> {

	List<MeetingElasticsearchOutbox> findByPublishedFalseOrderByCreatedAt();
}
