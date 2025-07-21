package com.example.momo.domain.payments.infra;

import com.example.momo.domain.payments.domain.Payment;
import com.example.momo.domain.payments.enums.PaymentStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {


  boolean existsByMeetingIdAndUserIdAndStatus(Long meetingId, Long userId, PaymentStatus status);

  List<Payment> findByMeetingId(Long meetingId);

  List<Payment> findByUserId(Long userId);
}
