package com.example.momo.domain.payment.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.momo.domain.payment.enums.PaymentStatus;

public interface PaymentRepository {

	Payment save(Payment payment);

	Optional<Payment> findById(Long id);

	void delete(Payment payment);

	// 단일 결제 조회 (unique constraint: meeting_id + user_id)
	Optional<Payment> findByMeetingIdAndUserIdAndStatus(Long meetingId, Long userId, PaymentStatus status);

	Optional<Payment> findByMeetingIdAndUserId(Long meetingId, Long userId);

	// 모임별 특정 상태의 모든 결제 조회 (모임 삭제 시 전체 환불용)
	List<Payment> findByMeetingIdAndStatus(Long meetingId, PaymentStatus status);

	Page<Payment> searchMyPayments(Long userId, PaymentStatus status, Pageable pageable);

}
