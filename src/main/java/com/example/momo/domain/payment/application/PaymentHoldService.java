package com.example.momo.domain.payment.application;

import com.example.momo.domain.payment.application.dto.CardPaymentTestRequestDto;
import com.example.momo.domain.payment.application.dto.PaymentResponseDto;

/**
 * 자리 보류(FAIL -> 보류 해제 & 재시도)
 */
public interface PaymentHoldService {

	/** 사용자가 직접 보류를 해제 */
	void releaseHold(Long userId, Long meetingId);

	/** FAILED -> 재시도(새로운 Key‑in) */
	PaymentResponseDto retryPayment(Long paymentId, CardPaymentTestRequestDto request, Long userId);
}
