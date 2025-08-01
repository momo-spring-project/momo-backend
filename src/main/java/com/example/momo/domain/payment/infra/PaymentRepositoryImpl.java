package com.example.momo.domain.payment.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.momo.domain.payment.domain.Payment;
import com.example.momo.domain.payment.domain.PaymentRepository;
import com.example.momo.domain.payment.enums.PaymentStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

	private final PaymentJpaRepository paymentJpaRepository;

	@Override
	public Payment save(Payment payment) {
		return paymentJpaRepository.save(payment);
	}

	@Override
	public Optional<Payment> findById(Long id) {
		return paymentJpaRepository.findById(id);
	}

	@Override
	public boolean existsByMeetingIdAndUserIdAndStatus(Long meetingId, Long userId,
		PaymentStatus status) {
		return paymentJpaRepository.existsByMeetingIdAndUserIdAndStatus(meetingId, userId, status);
	}

	@Override
	public List<Payment> findByMeetingId(Long meetingId) {
		return paymentJpaRepository.findByMeetingId(meetingId);
	}

	@Override
	public List<Payment> findByUserId(Long userId) {
		return paymentJpaRepository.findByUserId(userId);
	}

	@Override
	public void delete(Payment payment) {
		paymentJpaRepository.delete(payment);
	}

	@Override
	public Page<Payment> searchPayments(Long meetingId, Long userId, PaymentStatus status,
		Pageable pageable) {
		return paymentJpaRepository.searchPayments(meetingId, userId, status, pageable);
	}

	@Override
	public Optional<Payment> findByMeetingIdAndUserIdAndStatus(Long meetingId,
		Long userId,
		PaymentStatus status) {
		return paymentJpaRepository.findByMeetingIdAndUserIdAndStatus(meetingId,
			userId,
			status);
	}
}