package com.example.momo.domain.payment.infra;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.momo.domain.payment.domain.PaymentOutbox;
import com.example.momo.domain.payment.enums.OutboxStatus;

public interface PaymentOutboxJpaRepository extends JpaRepository<PaymentOutbox, Long> {

	// 30일 지난 PUBLISHED 청소
	@Modifying
	@Query("DELETE FROM PaymentOutbox o " +
		"WHERE o.status = 'PUBLISHED' AND o.publishedAt < :threshold")
	int deletePublishedBefore(LocalDateTime threshold);

	// DLQ 메시지 삭제
	@Modifying
	@Query("DELETE FROM PaymentOutbox o WHERE o.eventType LIKE 'DLQ_%' " +
		"AND o.createdAt < :threshold")
	int deleteDlqMessagesBefore(LocalDateTime threshold);

	// 발행 대기 중인 이벤트
	List<PaymentOutbox> findByStatusOrderByCreatedAt(OutboxStatus status, Pageable pageable);

	// 재시도 가능한 실패 이벤트
	List<PaymentOutbox> findByStatusAndRetryCountLessThan(OutboxStatus status, int maxRetry);

	// DLQ 대상 조회
	List<PaymentOutbox> findByStatusAndRetryCountGreaterThanEqual(OutboxStatus status, int retryCount);

}
