package com.example.momo.domain.payment.domain;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import com.example.momo.global.common.entity.BaseCreateEntity;
import com.example.momo.domain.payment.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_payment_meeting_user",
        columnNames = {"meeting_id", "user_id"}
    )
)
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Builder
public class Payment extends BaseCreateEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "meeting_id", nullable = false)
  private Long meetingId;

  @Column(nullable = false)
  private int amount; // Toss: totalAmount

  @Column(name = "payment_method", nullable = false)
  private String paymentMethod; // Toss: method

  @Column(name = "pg_transaction_id")
  private String pgTransactionId;  // Toss: paymentKey

  @Column(name = "order_id")
  private String orderId; // Toss: orderId

  @Enumerated(STRING)
  @Column(nullable = false)
  private PaymentStatus status;  // Toss: status (DONE, CANCELED 등)

  @Column(name = "paid_at")
  private LocalDateTime paidAt; // Toss: approvedAt

  // 낙관적 잠금을 위한 버전
  @Version
  private Long version;

  // V1: 무료 결제 생성 (참가비 0원)
  public static Payment createFree(Long userId, Long meetingId) {
    return Payment.builder()
        .userId(userId)
        .meetingId(meetingId)
        .amount(0)
        .paymentMethod("FREE")
        .status(PaymentStatus.COMPLETED)
        .paidAt(LocalDateTime.now())
        .build();
  }

  // 환불 처리
  public void refund() {
    if (this.status != PaymentStatus.COMPLETED) {
      throw new IllegalStateException("완료된 결제만 환불 가능합니다.");
    }
    this.status = PaymentStatus.REFUNDED;
  }
}