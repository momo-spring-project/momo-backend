package com.example.momo.global.webclient.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossKeyInRequestDto {
	private Integer amount;
	private String orderId;
	private String orderName;
	private String cardNumber;
	private String cardExpirationYear;
	private String cardExpirationMonth;
	private String cardPassword;
	private String customerIdentityNumber;
	private String customerEmail;
	private String customerName;
}
