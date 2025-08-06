package com.example.momo.domain.payment.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.momo.domain.payment.domain.Payment;
import com.example.momo.domain.payment.enums.PaymentStatus;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

	boolean existsByMeetingIdAndUserIdAndStatus(Long meetingId, Long userId, PaymentStatus status);

	List<Payment> findByMeetingId(Long meetingId);

	List<Payment> findByUserId(Long userId);

	@Query("SELECT p FROM Payment p WHERE " +
		"(:meetingId IS NULL OR p.meetingId = :meetingId) AND " +
		"(:userId IS NULL OR p.userId = :userId) AND " +
		"(:status IS NULL OR p.status = :status)")
	Page<Payment> searchPayments(@Param("meetingId") Long meetingId,
		@Param("userId") Long userId,
		@Param("status") PaymentStatus status,
		Pageable pageable);

	Optional<Payment> findByMeetingIdAndUserIdAndStatus(Long meetingId,
		Long userId,
		PaymentStatus status);

	Optional<Payment> findByMeetingIdAndUserId(Long meetingId, Long userId);

}
