package com.example.momo.domain.payment.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.momo.domain.payment.application.dto.CardPaymentTestRequestDto;
import com.example.momo.domain.payment.application.dto.PaymentResponseDto;
import com.example.momo.domain.payment.application.dto.RefundRequestDto;
import com.example.momo.domain.payment.enums.PaymentStatus;
import com.example.momo.global.webclient.payment.dto.TossPaymentResponseDto;

public interface PaymentService {

	//테스트 key-in 결제
	PaymentResponseDto createTestKeyInPayment(CardPaymentTestRequestDto request, Long userId);

	PaymentResponseDto createTestKeyInPayment(CardPaymentTestRequestDto dto, Long userId, String correlationUuid);

	//환불 처리
	PaymentResponseDto refundPayment(Long paymentId, Long userId, RefundRequestDto request, String correlationUuid);

	PaymentResponseDto refundPayment(Long paymentId, Long userId, RefundRequestDto request);

	//조회 메서드
	Page<PaymentResponseDto> getMyPayments(Long userId, PaymentStatus status, Pageable pageable);

	TossPaymentResponseDto getPgPayment(Long paymentId, Long userId);
}
