package com.example.momo.domain.payment.domain;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import java.time.LocalDateTime;

import com.example.momo.domain.payment.enums.PaymentStatus;
import com.example.momo.global.common.entity.BaseCreateEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
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

	@Column(name = "failed_at")
	private LocalDateTime failedAt;

	@Column(name = "refunded_at")
	private LocalDateTime refundedAt;

	@Column(name = "fail_reason")
	private String failReason;

	// 낙관적 잠금을 위한 버전
	@Version
	private Long version;

	// Private 생성자 - Builder 전용
	@Builder(access = AccessLevel.PRIVATE)  //  PRIVATE Builder
	private Payment(Long userId, Long meetingId, int amount,
		String paymentMethod, PaymentStatus status,
		LocalDateTime paidAt) {
		this.userId = userId;
		this.meetingId = meetingId;
		this.amount = amount;
		this.paymentMethod = paymentMethod;
		this.status = status;
		this.paidAt = paidAt;
	}

	// PENDING 결제 생성 (결제 시작)
	public static Payment createPending(Long userId, Long meetingId, int amount) {
		return Payment.builder()
			.userId(userId)
			.meetingId(meetingId)
			.amount(amount)
			.paymentMethod("TOSS")
			.status(PaymentStatus.PENDING)
			.build();
	}

	// 결제 완료 처리
	public void complete(String pgTransactionId, String orderId, LocalDateTime paidAt) {
		if (this.status != PaymentStatus.PENDING) {
			throw new IllegalStateException("PENDING 상태에서만 완료 처리가 가능합니다.");
		}
		this.status = PaymentStatus.COMPLETED;
		this.pgTransactionId = pgTransactionId;
		this.orderId = orderId;
		this.paidAt = paidAt;
	}

	// 결제 실패 처리
	public void fail(String reason) {
		if (this.status == PaymentStatus.COMPLETED || this.status == PaymentStatus.REFUNDED) {
			throw new IllegalStateException("완료되거나 환불된 결제는 실패 처리할 수 없습니다.");
		}
		this.status = PaymentStatus.FAILED;
		this.failedAt = LocalDateTime.now();
		this.failReason = reason;
	}

	// 환불 처리 (완료된 결제를 환불)
	public void refund() {
		if (this.status != PaymentStatus.COMPLETED) {
			throw new IllegalStateException("완료된 결제만 환불 가능합니다.");
		}
		this.status = PaymentStatus.REFUNDED;
		this.refundedAt = LocalDateTime.now();
	}

	// 상태 전이 메서드 추가
	public void reopenPending() {
		if (this.status != PaymentStatus.FAILED) {
			throw new IllegalStateException("FAILED 상태에서만 PENDING으로 전환할 수 있습니다.");
		}
		this.status = PaymentStatus.PENDING;
		this.failedAt = null;
		this.failReason = null;
	}
}