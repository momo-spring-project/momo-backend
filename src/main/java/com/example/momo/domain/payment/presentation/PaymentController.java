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
import com.example.momo.domain.payment.domain.PaymentService;
import com.example.momo.domain.payment.application.dto.CardPaymentTestRequestDto;
import com.example.momo.domain.payment.application.dto.PaymentResponseDto;
import com.example.momo.domain.payment.application.dto.RefundRequestDto;
import com.example.momo.domain.payment.enums.PaymentStatus;
import com.example.momo.global.common.dto.ApiResponse;
import com.example.momo.global.webclient.payment.dto.TossPaymentResponseDto;

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

	//내 결제 내역 조회
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<Page<PaymentResponseDto>>> getMyPayments(
		@AuthenticationPrincipal AuthUser authUser,
		@RequestParam(required = false) PaymentStatus status,
		Pageable pageable
	) {
		Page<PaymentResponseDto> page = paymentService.getMyPayments(authUser.getId(), status, pageable);
		return ResponseEntity.ok(ApiResponse.success("내 결제 내역 조회 완료", page));
	}

	//토스 조회 api 활용 내 결제 내역 조회
	@GetMapping("/{paymentId}/pg")
	public ResponseEntity<ApiResponse<TossPaymentResponseDto>> getPgPayment(
		@PathVariable Long paymentId,
		@AuthenticationPrincipal AuthUser authUser
	) {
		TossPaymentResponseDto dto = paymentService.getPgPayment(paymentId, authUser.getId());
		return ResponseEntity.ok(ApiResponse.success("PG 결제 조회 완료", dto));
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