package com.example.momo.domain.payment.application;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.momo.domain.payment.application.dto.CardPaymentTestRequestDto;
import com.example.momo.domain.payment.application.dto.PaymentResponseDto;
import com.example.momo.domain.payment.application.dto.RefundRequestDto;
import com.example.momo.domain.payment.enums.PaymentStatus;

public interface PaymentService {

	PaymentResponseDto createTestKeyInPayment(CardPaymentTestRequestDto request, Long userId);

	//테스트 key-in 결제
	PaymentResponseDto createTestKeyInPayment(CardPaymentTestRequestDto dto, Long userId, String correlationUuid);

	//환불 처리
	PaymentResponseDto refundPayment(Long paymentId, Long userId, RefundRequestDto request, String correlationUuid);

	PaymentResponseDto refundPayment(Long paymentId, Long userId, RefundRequestDto request);

	//조회 메서드들
	List<PaymentResponseDto> getPaymentsByMeetingId(Long meetingId);

	List<PaymentResponseDto> getPaymentsByUserId(Long userId);

	Page<PaymentResponseDto> getMyPayments(Long userId, PaymentStatus status, Pageable pageable);

	boolean validateUserPayment(Long userId, Long meetingId);

	//관리자 용 조회 메서드
	// Page<PaymentResponseDto> searchPayments(Long meetingId,
	// 	Long userId,
	// 	PaymentStatus status,
	// 	Pageable pageable);

}
