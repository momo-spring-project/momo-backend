package com.example.momo.domain.payment.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardPaymentTestRequestDto {

	@NotNull(message = "모임 ID는 필수입니다")
	private Long meetingId;

	@Pattern(regexp = "\\d{16}", message = "카드번호는 16자리 숫자여야 합니다")
	private String cardNumber;

	@Pattern(regexp = "\\d{2}/\\d{2}", message = "카드 유효기간은 MM/YY 형식이어야 합니다")
	private String cardExpiry;

	@Pattern(regexp = "\\d{3}", message = "CVC는 3자리 숫자여야 합니다")
	private String cardCvc;

	@Pattern(regexp = "\\d{6}", message = "생년월일은 6자리 숫자여야 합니다")
	private String birth;
}
