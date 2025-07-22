package com.example.momo.domain.payments.application;

import com.example.momo.domain.payments.dto.CardPaymentTestRequest;
import com.example.momo.domain.payments.dto.PaymentResponse;
import com.example.momo.domain.payments.dto.RefundRequest;
import java.util.List;

public interface PaymentService {


  //테스트 key-in 결제
  PaymentResponse createTestKeyInPayment(CardPaymentTestRequest dto, Long userId);

  //환불 처리
  PaymentResponse refundPayment(Long paymentId, Long userId, RefundRequest request);

  //조회 메서드들
  List<PaymentResponse> getPaymentsByMeetingId(Long meetingId);

  List<PaymentResponse> getPaymentsByUserId(Long userId);

  boolean hasUserPaidForMeeting(Long userId, Long meetingId);

}
