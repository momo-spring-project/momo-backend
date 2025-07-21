package com.example.momo.domain.payments.infra;

import com.example.momo.domain.payments.domain.Payment;
import com.example.momo.domain.payments.enums.PaymentStatus;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}