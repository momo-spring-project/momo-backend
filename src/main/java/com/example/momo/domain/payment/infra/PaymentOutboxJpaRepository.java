package com.example.momo.domain.payment.infra;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.momo.domain.payment.domain.PaymentOutbox;

public interface PaymentOutboxJpaRepository extends JpaRepository<PaymentOutbox, Long> {

	/** (스케줄러) PENDING ID 픽업 - 선점/락 없음 */
	@Query(value = """
		SELECT id FROM payment_outbox
		WHERE status = 'PENDING'
		ORDER BY created_at ASC
		LIMIT :limit
		""", nativeQuery = true)
	List<Long> pickPendingIds(@Param("limit") int limit);

	/** (스케줄러) 재시도 가능 FAILED ID 픽업 - 선점/락 없음 */
	@Query(value = """
		SELECT id FROM payment_outbox
		WHERE status = 'FAILED'
		  AND retry_count < :maxRetry
		  AND (next_retry_at IS NULL OR next_retry_at <= NOW())
		ORDER BY IFNULL(next_retry_at, '1970-01-01') ASC, created_at ASC
		LIMIT :limit
		""", nativeQuery = true)
	List<Long> pickRetryableFailedIds(@Param("maxRetry") int maxRetry,
		@Param("limit") int limit);

	/** 단건 이벤트 선점 시도 (원자적) */
	@Modifying
	@Query("""
		UPDATE PaymentOutbox o
		   SET o.status = 'PROCESSING',
		       o.updatedAt = :now
		 WHERE o.id = :id
		   AND o.status IN ('PENDING','FAILED')
		   AND (o.status = 'PENDING' OR (o.nextRetryAt IS NULL OR o.nextRetryAt <= :now))
		""")
	int tryMarkProcessing(@Param("id") Long id, @Param("now") LocalDateTime now);

	/** 오래된 PUBLISHED 메시지 삭제 */
	@Modifying
	@Query("""
		DELETE FROM PaymentOutbox o
		 WHERE o.status = 'PUBLISHED'
		   AND o.publishedAt < :threshold
		""")
	int deletePublishedBefore(@Param("threshold") LocalDateTime threshold);

	/** 오래된 DLQ 메시지 삭제 */
	@Modifying
	@Query("""
		DELETE FROM PaymentOutbox o
		 WHERE o.status = 'DEAD_LETTERED'
		   AND o.createdAt < :threshold
		""")
	int deleteDlqMessagesBefore(@Param("threshold") LocalDateTime threshold);

	/** 방치된 PROCESSING 이벤트 복구 */
	@Modifying
	@Query(value = """
		UPDATE payment_outbox
		   SET status = 'FAILED',
		       updated_at = NOW(),
		       failure_reason = 'Processing timeout',
		       next_retry_at = DATE_ADD(NOW(), INTERVAL (10 * POW(2, retry_count)) SECOND)
		 WHERE status = 'PROCESSING'
		   AND updated_at < DATE_SUB(NOW(), INTERVAL :minutes MINUTE)
		""", nativeQuery = true)
	int recoverStuckProcessingEvents(@Param("minutes") int minutes);
}
