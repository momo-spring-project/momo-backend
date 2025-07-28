package com.example.momo.domain.payment.domain.dto;

import java.time.LocalDateTime;

import com.example.momo.domain.payment.domain.Payment;

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
	private Long meetingId;
	private Long userId;
	private int amount;
	private String paymentMethod;
	private String status;
	private LocalDateTime paidAt;
	private LocalDateTime createdAt;

	public static PaymentResponseDto from(Payment payment) {
		return PaymentResponseDto.builder()
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