package com.example.momo.domain.payment.application;

import com.example.momo.domain.payment.domain.dto.CardPaymentTestRequest;
import com.example.momo.domain.payment.domain.dto.PaymentResponse;
import com.example.momo.domain.payment.domain.dto.RefundRequest;
import com.example.momo.domain.payment.enums.PaymentStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {


  //테스트 key-in 결제
  PaymentResponse createTestKeyInPayment(CardPaymentTestRequest dto, Long userId);

  //환불 처리
  PaymentResponse refundPayment(Long paymentId, Long userId, RefundRequest request);

  //조회 메서드들
  List<PaymentResponse> getPaymentsByMeetingId(Long meetingId);

  List<PaymentResponse> getPaymentsByUserId(Long userId);

  boolean hasUserPaidForMeeting(Long userId, Long meetingId);


  Page<PaymentResponse> searchPayments(Long meetingId,
      Long userId,
      PaymentStatus status,
      Pageable pageable);

}
