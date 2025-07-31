package com.example.momo.domain.payment.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardPaymentTestRequestDto {

	@NotNull
	private Long meetingId;

	private String cardNumber;   // 기본: 4242424242424242
	private String cardExpiry;   // 12/25
	private String cardCvc;      // 242
	private String birth;        // 881212
}