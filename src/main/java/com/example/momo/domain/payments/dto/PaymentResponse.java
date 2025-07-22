package com.example.momo.domain.payments.dto;

import com.example.momo.domain.payments.domain.Payment;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

  private Long id;
  private Long meetingId;
  private Long userId;
  private int amount;
  private String paymentMethod;
  private String status;
  private LocalDateTime paidAt;
  private LocalDateTime createdAt;


  public static PaymentResponse from(Payment payment) {
    return PaymentResponse.builder()
        .id(payment.getId())
        .meetingId(payment.getMeetingId())
        .userId(payment.getUserId())
        .amount(payment.getAmount())
        .paymentMethod(payment.getPaymentMethod())
        .status(payment.getStatus().name())
        .paidAt(payment.getPaidAt())
        .createdAt(payment.getCreatedAt())
        .build();
  }
}