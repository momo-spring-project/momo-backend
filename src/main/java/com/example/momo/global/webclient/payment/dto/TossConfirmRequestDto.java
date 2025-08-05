package com.example.momo.global.webclient.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossConfirmRequestDto {
	private String paymentKey;
	private String orderId;
	private Integer amount;
}

