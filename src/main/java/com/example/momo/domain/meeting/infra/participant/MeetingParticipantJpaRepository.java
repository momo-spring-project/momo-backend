package com.example.momo.domain.meeting.infra.participant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.momo.domain.meeting.domain.MeetingParticipant;

public interface MeetingParticipantJpaRepository extends JpaRepository<MeetingParticipant, Long> {
	boolean existsByMeetingIdAndUserId(Long meetingId, Long userId);

	@Query("SELECT mp FROM MeetingParticipant mp WHERE mp.meetingId = :meetingId")
	List<MeetingParticipant> findAllByMeetingId(@Param("meetingId") Long meetingId);

	Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);

	// === 유저 도메인에서 사용 (클라이언트 통신으로 수정 후 삭제 예정) ===

	/**
	 * 특정 사용자가 참가한 총 모임 수
	 */
	long countByUserId(Long userId);

	/**
	 * 특정 사용자가 실제 참석한 모임 수 (attendanceStatus = true)
	 */
	long countByUserIdAndAttendanceStatusTrue(Long userId);

	/**
	 * 특정 날짜 이후 특정 사용자가 참가한 모임 수 (최근 활동도 계산용)
	 */
	@Query("SELECT COUNT(mp) FROM MeetingParticipant mp WHERE mp.userId = :userId AND mp.createdAt >= :afterDate")
	long countByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("afterDate") LocalDateTime afterDate);
}
