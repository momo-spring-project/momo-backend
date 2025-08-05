package com.example.momo.domain.payment.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestDto {

	@NotBlank(message = "환불 사유는 필수입니다")
	private String reason;
}