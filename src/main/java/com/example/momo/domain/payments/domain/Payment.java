package com.example.momo.domain.payments.domain;

import com.example.momo.domain.common.entity.BaseCreateEntity;
import com.example.momo.domain.payments.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

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
	private int amount;

	@Column(name = "payment_method", nullable = false)
	private String paymentMethod;

	@Column(name = "pg_transaction_id")
	private String pgTransactionId;

	@Enumerated(STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@Column(name = "paid_at")
	private LocalDateTime paidAt;

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

	// V1: 유료 결제 생성 (모의 결제로 바로 완료)
	public static Payment createPaid(Long userId, Long meetingId int amount, String paymentMethod) {
		return Payment.builder()
				.userId(userId)
				.meetingId(meetingId)
				.amount(amount)
				.paymentMethod(paymentMethod)
				.status(PaymentStatus.COMPLETED)
				.pgTransactionId("MOCK_" + System.currentTimeMillis())
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