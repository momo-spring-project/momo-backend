package com.example.momo.domain.payments.infra;

import com.example.momo.domain.payments.domain.Payment;
import com.example.momo.domain.payments.enums.PaymentStatus;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

  Payment save(Payment payment);

  Optional<Payment> findById(Long id);

  boolean existsByMeetingIdAndUserIdAndStatus(Long mId, Long uId, PaymentStatus st);

  List<Payment> findByMeetingId(Long meetingId);

  List<Payment> findByUserId(Long userId);

  void delete(Payment payment);
  
}
