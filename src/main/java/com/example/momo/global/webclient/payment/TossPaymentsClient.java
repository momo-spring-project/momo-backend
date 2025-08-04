package com.example.momo.global.webclient.payment;

import java.time.Duration;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.momo.domain.payment.application.PaymentClient;
import com.example.momo.global.webclient.payment.dto.TossCancelRequestDto;
import com.example.momo.global.webclient.payment.dto.TossConfirmRequestDto;
import com.example.momo.global.webclient.payment.dto.TossKeyInRequestDto;
import com.example.momo.global.webclient.payment.dto.TossPaymentResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentsClient implements PaymentClient {

	private final WebClient tossWebClient;
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

	// ==================== 결제 처리 API ====================

	@Override
	public TossPaymentResponseDto confirmPayment(String paymentKey, String orderId, int amount) {
		String url = "/payments/confirm";

		TossConfirmRequestDto request = TossConfirmRequestDto.builder()
			.paymentKey(paymentKey)
			.orderId(orderId)
			.amount(amount)
			.build();

		try {
			TossPaymentResponseDto response = tossWebClient
				.post()
				.uri(url)
				.header("Idempotency-Key", "confirm-" + orderId)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(TossPaymentResponseDto.class)
				.block(REQUEST_TIMEOUT);

			log.info("[TOSS] 결제 승인 완료 - orderId: {}, amount: {}", orderId, amount);
			return response;
		} catch (WebClientResponseException e) {
			log.error("[TOSS] 결제 승인 실패: {}", e.getResponseBodyAsString());
			throw e;
		}
	}

	@Override
	public TossPaymentResponseDto cancelPayment(String paymentKey, String reason) {
		String url = "/payments/{paymentKey}/cancel";

		TossCancelRequestDto request = TossCancelRequestDto.builder()
			.cancelReason(reason)
			.build();

		try {
			TossPaymentResponseDto response = tossWebClient
				.post()
				.uri(url, paymentKey)
				.header("Idempotency-Key", "cancel-" + paymentKey)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(TossPaymentResponseDto.class)
				.block(REQUEST_TIMEOUT);

			log.info("[TOSS] 결제 취소 완료 - paymentKey: {}, reason: {}", paymentKey, reason);
			return response;
		} catch (WebClientResponseException e) {
			log.error("[TOSS] 결제 취소 실패: {}", e.getResponseBodyAsString());
			throw e;
		}
	}

	// ==================== 조회 API ====================

	@Override
	public TossPaymentResponseDto getPayment(String paymentKey) {
		String url = "/payments/{paymentKey}";

		try {
			TossPaymentResponseDto result = tossWebClient
				.get()
				.uri(url, paymentKey)
				.retrieve()
				.bodyToMono(TossPaymentResponseDto.class)
				.block(REQUEST_TIMEOUT);

			log.debug("[TOSS] 결제 조회 - paymentKey: {}", paymentKey);
			return result;
		} catch (WebClientResponseException e) {
			log.error("[TOSS] 결제 조회 실패: {}", e.getResponseBodyAsString());
			throw e;
		}
	}

	// ==================== 테스트 전용 API ====================

	@Override
	public TossPaymentResponseDto createTestKeyInPayment(TossKeyInRequestDto request, String orderId) {
		String url = "/payments/key-in";

		try {
			TossPaymentResponseDto response = tossWebClient
				.post()
				.uri(url)
				.header("Idempotency-Key", "keyin-" + orderId)
				.bodyValue(request)
				.retrieve()
				.bodyToMono(TossPaymentResponseDto.class)
				.block(REQUEST_TIMEOUT);

			log.info("[TOSS] Key-in 결제 생성 완료 - orderId: {}", orderId);
			return response;
		} catch (WebClientResponseException e) {
			log.error("[TOSS] Key-in 결제 실패 - orderId: {}, error: {}", orderId, e.getResponseBodyAsString());
			throw e;
		} catch (Exception e) {
			log.error("[TOSS] Key-in 결제 실패 - orderId: {}, error: {}", orderId, e.getMessage());
			throw e;
		}
	}
}