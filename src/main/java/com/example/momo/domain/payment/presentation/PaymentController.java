package com.example.momo.domain.payment.presentation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.momo.domain.auth.application.dto.AuthUser;
import com.example.momo.domain.payment.application.PaymentService;
import com.example.momo.domain.payment.application.dto.CardPaymentTestRequestDto;
import com.example.momo.domain.payment.application.dto.PaymentResponseDto;
import com.example.momo.domain.payment.application.dto.RefundRequestDto;
import com.example.momo.domain.payment.enums.PaymentStatus;
import com.example.momo.global.common.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	//테스트 키인 결제 (카드번호 직접 입력)
	@PostMapping("/test/keyin")
	public ResponseEntity<ApiResponse<PaymentResponseDto>> createTestKeyInPayment(
		@RequestBody CardPaymentTestRequestDto request,
		@AuthenticationPrincipal AuthUser authUser) {

		PaymentResponseDto response = paymentService.createTestKeyInPayment(request, authUser.getId());
		return ResponseEntity.ok(ApiResponse.success("테스트 키인 결제가 완료되었습니다.", response));
	}

	/**
	 * 관리자용 다중 조건 검색 ex)
	 * /search?meetingId=1&userId=3&status=COMPLETED&page=0&size=20&sort=paidAt,desc
	 */
	@GetMapping("/search")
	public ResponseEntity<ApiResponse<Page<PaymentResponseDto>>> searchPayments(
		@RequestParam(required = false) Long meetingId,
		@RequestParam(required = false) Long userId,
		@RequestParam(required = false) PaymentStatus status,
		Pageable pageable) {

		Page<PaymentResponseDto> page = paymentService.searchPayments(meetingId, userId, status, pageable);
		return ResponseEntity.ok(ApiResponse.success("결제 내역 검색 완료", page));
	}

	// 환불 처리
	@PostMapping("/{paymentId}/refund")
	public ResponseEntity<ApiResponse<PaymentResponseDto>> refundPayment(
		@PathVariable Long paymentId,
		@RequestBody RefundRequestDto request,
		@AuthenticationPrincipal AuthUser authUser) {
		PaymentResponseDto response = paymentService.refundPayment(paymentId, authUser.getId(), request);
		return ResponseEntity.ok(ApiResponse.success("환불이 완료되었습니다.", response));
	}

}