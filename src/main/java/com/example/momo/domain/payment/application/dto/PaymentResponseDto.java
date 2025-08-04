// PaymentResponseDto.java - 기본 응답 DTO
package com.example.momo.domain.payment.application.dto;

import java.time.LocalDateTime;

import com.example.momo.domain.payment.domain.Payment;
import com.example.momo.domain.payment.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {

	private Long id;
	private Long userId;
	private Long meetingId;
	private Integer amount;
	private String paymentMethod;
	private PaymentStatus status;
	private LocalDateTime createdAt;

	// 상태별 상세 정보
	private PaymentDetailDto detail;

	public static PaymentResponseDto from(Payment payment) {
		PaymentResponseDtoBuilder builder = PaymentResponseDto.builder()
			.id(payment.getId())
			.userId(payment.getUserId())
			.meetingId(payment.getMeetingId())
			.amount(payment.getAmount())
			.paymentMethod(payment.getPaymentMethod())
			.status(payment.getStatus())
			.createdAt(payment.getCreatedAt());

		// 상태별 상세 정보 설정
		switch (payment.getStatus()) {
			case COMPLETED:
				builder.detail(PaymentDetailDto.completed(
					payment.getPgTransactionId(),
					payment.getOrderId(),
					payment.getPaidAt()
				));
				break;
			case FAILED:
			case CANCELED:
			case EXPIRED:
				builder.detail(PaymentDetailDto.failed(
					payment.getFailReason(),
					payment.getFailedAt() != null ? payment.getFailedAt() : payment.getCanceledAt()
				));
				break;
			case REFUNDED:
				builder.detail(PaymentDetailDto.refunded(
					payment.getPgTransactionId(),
					payment.getRefundedAt()
				));
				break;
			case PENDING:
			default:
				builder.detail(null);
		}

		return builder.build();
	}
}
