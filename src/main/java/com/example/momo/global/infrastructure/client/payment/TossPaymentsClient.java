package com.example.momo.global.infrastructure.client.payment;

import java.time.Duration;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.momo.domain.payment.application.PaymentClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentsClient implements PaymentClient {

	private final WebClient tossWebClient;
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

	// ==================== 결제 처리 API ====================

	public Map<String, Object> confirmPayment(String paymentKey, String orderId, int amount) {
		String url = "/payments/confirm";

		Map<String, Object> body = Map.of(
			"paymentKey", paymentKey,
			"orderId", orderId,
			"amount", amount
		);

		try {
			Map<String, Object> response = tossWebClient
				.post()
				.uri(url)
				.header("Idempotency-Key", "confirm-" + orderId)
				.bodyValue(body)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
				})
				.block(REQUEST_TIMEOUT);

			log.info("[TOSS] 결제 승인 완료 - orderId: {}, amount: {}", orderId, amount);
			return response;
		} catch (WebClientResponseException e) {
			log.error("[TOSS] 결제 승인 실패: {}", e.getResponseBodyAsString());
			throw e;
		}
	}

	public Map<String, Object> cancelPayment(String paymentKey, String reason) {
		String url = "/payments/{paymentKey}/cancel";

		Map<String, Object> body = Map.of("cancelReason", reason);

		try {
			Map<String, Object> response = tossWebClient
				.post()
				.uri(url, paymentKey)
				.header("Idempotency-Key", "cancel-" + paymentKey)
				.bodyValue(body)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
				})
				.block(REQUEST_TIMEOUT);

			log.info("[TOSS] 결제 취소 완료 - paymentKey: {}, reason: {}", paymentKey, reason);
			return response;
		} catch (WebClientResponseException e) {
			log.error("[TOSS] 결제 취소 실패: {}", e.getResponseBodyAsString());
			throw e;
		}
	}

	// ==================== 조회 API ====================

	public Map<String, Object> getPayment(String paymentKey) {
		String url = "/payments/{paymentKey}";

		try {
			Map<String, Object> result = tossWebClient
				.get()
				.uri(url, paymentKey)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
				})
				.block(REQUEST_TIMEOUT);

			log.debug("[TOSS] 결제 조회 - paymentKey: {}", paymentKey);
			return result;
		} catch (WebClientResponseException e) {
			log.error("[TOSS] 결제 조회 실패: {}", e.getResponseBodyAsString());
			throw e;
		}
	}

	// ==================== 테스트 전용 API ====================

	public Map<String, Object> createTestKeyInPayment(Map<String, Object> payload, String orderId) {
		String url = "/payments/key-in";  // 토스 API 문서에 따른 엔드포인트

		try {
			Map<String, Object> response = tossWebClient
				.post()
				.uri(url)
				.header("Idempotency-Key", "keyin-" + orderId)
				.bodyValue(payload)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
				})
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