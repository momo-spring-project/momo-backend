package com.example.momo.domain.payments.enums;

public enum PaymentStatus {
  COMPLETED("결제 완료"),  // V1에서는 바로 완료 처리
  REFUNDED("환불 완료");   // 환불된 상태

  private final String description;

  PaymentStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}