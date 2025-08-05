// PaymentDetailDto.java - 상태별 상세 정보
package com.example.momo.domain.payment.application.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentDetailDto {

	// 완료된 결제 정보
	private String pgTransactionId;
	private String orderId;
	private LocalDateTime paidAt;

	// 실패/취소된 결제 정보
	private String failReason;
	private LocalDateTime failedAt;

	// 환불된 결제 정보
	private LocalDateTime refundedAt;

	public static PaymentDetailDto completed(String pgTransactionId, String orderId, LocalDateTime paidAt) {
		return PaymentDetailDto.builder()
			.pgTransactionId(pgTransactionId)
			.orderId(orderId)
			.paidAt(paidAt)
			.build();
	}

	public static PaymentDetailDto failed(String failReason, LocalDateTime failedAt) {
		return PaymentDetailDto.builder()
			.failReason(failReason)
			.failedAt(failedAt)
			.build();
	}

	public static PaymentDetailDto refunded(String pgTransactionId, LocalDateTime refundedAt) {
		return PaymentDetailDto.builder()
			.pgTransactionId(pgTransactionId)
			.refundedAt(refundedAt)
			.build();
	}
}