package com.example.momo.global.webclient.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossCancelRequestDto {
	private String cancelReason;
	private Integer cancelAmount;  // null이면 전액 취소
}