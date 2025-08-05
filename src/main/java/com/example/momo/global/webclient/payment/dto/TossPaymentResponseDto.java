package com.example.momo.global.webclient.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossPaymentResponseDto {
	private String paymentKey;
	private String orderId;
	private String orderName;
	private String status;
	private Integer totalAmount;
	private String method;
	private String approvedAt;
	private String requestedAt;

	// 실패 정보
	private TossFailure failure;

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class TossFailure {
		private String code;
		private String message;
	}
}
